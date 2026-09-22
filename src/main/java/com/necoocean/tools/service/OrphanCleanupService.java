package com.necoocean.tools.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.necoocean.tools.config.CosProperties;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.entity.ResourceFile;
import com.necoocean.tools.domain.repository.ResourceFileRepository;
import com.necoocean.tools.dto.admin.OrphanCleanupResultDto;
import com.necoocean.tools.service.cos.CosObjectStore;
import com.necoocean.tools.service.cos.CosObjectSummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 孤儿对象清理。删「库无记录且超过判定窗口」的 COS 对象，以及超时未完成的上传占位。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class OrphanCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(OrphanCleanupService.class);

    private final ResourceFileRepository resourceFiles;

    private final CosObjectStore cosObjectStore;

    private final CosProperties cosProperties;

    /**
     * @param resourceFiles  资源文件
     * @param cosObjectStore 对象存储
     * @param cosProperties  判定窗口
     */
    public OrphanCleanupService(ResourceFileRepository resourceFiles, CosObjectStore cosObjectStore,
            CosProperties cosProperties) {
        this.resourceFiles = resourceFiles;
        this.cosObjectStore = cosObjectStore;
        this.cosProperties = cosProperties;
    }

    /**
     * 执行一次清理。
     *
     * @return 清理统计
     */
    @Transactional(rollbackFor = Exception.class)
    public OrphanCleanupResultDto cleanup() {
        int deletedPending = cleanupStalePending();
        int deletedObjects = cleanupOrphanObjects();
        logger.info("orphan cleanup done, deletedObjects={}, deletedPendingRows={}", deletedObjects, deletedPending);
        return new OrphanCleanupResultDto(Integer.valueOf(deletedObjects), Integer.valueOf(deletedPending));
    }

    private int cleanupStalePending() {
        LocalDateTime before = EntityTimestamps.now().minusHours(cosProperties.getOrphanAgeHours());
        List<ResourceFile> stale = resourceFiles.findPendingOlderThan(
                Integer.valueOf(ResourceFile.OBJECT_STATUS_PENDING), before);
        int deleted = 0;
        for (ResourceFile file : stale) {
            cosObjectStore.deleteObject(file.getObjectKey());
            resourceFiles.delete(file);
            deleted++;
        }
        if (deleted > 0) {
            resourceFiles.flush();
        }
        return deleted;
    }

    private int cleanupOrphanObjects() {
        if (!cosObjectStore.available()) {
            return 0;
        }
        Instant olderThan = Instant.now().minus(cosProperties.getOrphanAgeHours(), ChronoUnit.HOURS);
        Set<String> knownKeys = new HashSet<String>(resourceFiles.findAllObjectKeys());
        int deleted = 0;
        for (CosObjectSummary summary : cosObjectStore.listObjectsOlderThan(olderThan)) {
            if (!knownKeys.contains(summary.getObjectKey())) {
                cosObjectStore.deleteObject(summary.getObjectKey());
                deleted++;
            }
        }
        return deleted;
    }
}

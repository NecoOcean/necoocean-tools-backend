package com.necoocean.tools.service;

import com.necoocean.tools.domain.entity.ResourceFile;
import com.necoocean.tools.domain.repository.ResourceFileRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资源文件状态短事务。异常标记须在下载/登记失败回滚之外单独提交。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class ResourceFileStatusService {

    private final ResourceFileRepository resourceFiles;

    /**
     * @param resourceFiles 资源文件
     */
    public ResourceFileStatusService(ResourceFileRepository resourceFiles) {
        this.resourceFiles = resourceFiles;
    }

    /**
     * 标记对象缺失。独立事务，避免被外层 BizException 回滚。
     *
     * @param fileId 文件主键
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markAbnormal(Integer fileId) {
        ResourceFile file = resourceFiles.findById(fileId).orElse(null);
        if (file == null) {
            return;
        }
        file.setObjectStatus(Integer.valueOf(ResourceFile.OBJECT_STATUS_ABNORMAL));
        resourceFiles.saveAndFlush(file);
    }
}

package com.necoocean.tools.service;

import java.util.List;
import java.util.Set;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.entity.ResourceFile;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.repository.ResourceFileRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import com.necoocean.tools.dto.admin.IsLatestRequest;
import com.necoocean.tools.dto.admin.ResourceFileAdminDto;
import com.necoocean.tools.dto.admin.ResourceFileUpdateRequest;
import com.necoocean.tools.service.cos.CosObjectStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 后台资源文件元信息。设推荐版本在事务内先清后置，并同步 tools.latest_version。
 * 删除时同步删对象存储。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class ResourceFileAdminService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceFileAdminService.class);

    private static final int VERSION_MAX = 32;

    private static final int DISPLAY_NAME_MAX = 255;

    private static final Set<String> PLATFORMS = Set.of("Windows", "Android", "Mac", "通用");

    private final ResourceFileRepository resourceFiles;

    private final ToolRepository tools;

    private final CosObjectStore cosObjectStore;

    /**
     * @param resourceFiles  资源文件
     * @param tools          工具
     * @param cosObjectStore 对象存储
     */
    public ResourceFileAdminService(ResourceFileRepository resourceFiles, ToolRepository tools,
            CosObjectStore cosObjectStore) {
        this.resourceFiles = resourceFiles;
        this.tools = tools;
        this.cosObjectStore = cosObjectStore;
    }

    /**
     * 编辑文件元信息。不可改 object_key。
     *
     * @param id      文件主键
     * @param request 请求体
     * @return 更新后的元信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ResourceFileAdminDto update(Integer id, ResourceFileUpdateRequest request) {
        ResourceFile file = requireFile(id);
        if (request == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (StringUtils.hasText(request.getDisplayName())) {
            file.setDisplayName(requireDisplayName(request.getDisplayName()));
        }
        if (request.getPlatform() != null) {
            file.setPlatform(normalizePlatform(request.getPlatform()));
        }
        if (StringUtils.hasText(request.getVersion())) {
            file.setVersion(requireVersion(request.getVersion()));
        }
        resourceFiles.saveAndFlush(file);
        if (request.isLatestPresent()) {
            if (Boolean.TRUE.equals(request.getLatest())) {
                markLatest(file);
            } else {
                clearLatestIfNeeded(file);
            }
        }
        ResourceFile refreshed = requireFile(id);
        logger.info("resource file updated, id={}", id);
        return toDto(refreshed);
    }

    /**
     * 设为推荐版本。事务内先清后置，并同步 tools.latest_version。
     *
     * @param id      文件主键
     * @param request 是否推荐
     * @return 更新后的元信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ResourceFileAdminDto patchLatest(Integer id, IsLatestRequest request) {
        ResourceFile file = requireFile(id);
        if (request == null || request.getLatest() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (Boolean.TRUE.equals(request.getLatest())) {
            markLatest(file);
        } else {
            clearLatestIfNeeded(file);
        }
        ResourceFile refreshed = requireFile(id);
        logger.info("resource file latest patched, id={}, latest={}", id, request.getLatest());
        return toDto(refreshed);
    }

    /**
     * 删除文件：先删对象，再删库。若删的是推荐版本，自动指定同工具最新就绪文件为推荐。
     *
     * @param id 文件主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        ResourceFile file = requireFile(id);
        Integer toolId = file.getTool().getId();
        boolean wasLatest = Integer.valueOf(ResourceFile.LATEST).equals(file.getLatest());
        String objectKey = file.getObjectKey();
        cosObjectStore.deleteObject(objectKey);
        resourceFiles.delete(file);
        resourceFiles.flush();
        if (wasLatest) {
            promoteNewest(toolId);
        }
        logger.info("resource file deleted, id={}, key={}", id, objectKey);
    }

    private void markLatest(ResourceFile file) {
        Integer toolId = file.getTool().getId();
        resourceFiles.clearLatestByToolId(toolId, Integer.valueOf(ResourceFile.NOT_LATEST));
        ResourceFile target = requireFile(file.getId());
        target.setLatest(Integer.valueOf(ResourceFile.LATEST));
        resourceFiles.saveAndFlush(target);
        Tool tool = tools.findById(toolId).orElseThrow(() -> new BizException(ErrorCode.TOOL_NOT_FOUND));
        tool.setLatestVersion(target.getVersion());
        tools.saveAndFlush(tool);
    }

    private void clearLatestIfNeeded(ResourceFile file) {
        if (!Integer.valueOf(ResourceFile.LATEST).equals(file.getLatest())) {
            return;
        }
        Integer toolId = file.getTool().getId();
        file.setLatest(Integer.valueOf(ResourceFile.NOT_LATEST));
        resourceFiles.saveAndFlush(file);
        Tool tool = tools.findById(toolId).orElseThrow(() -> new BizException(ErrorCode.TOOL_NOT_FOUND));
        tool.setLatestVersion(null);
        tools.saveAndFlush(tool);
    }

    private void promoteNewest(Integer toolId) {
        List<ResourceFile> remaining = resourceFiles.findByToolIdOrderByCreatedAtDesc(toolId);
        Tool tool = tools.findById(toolId).orElseThrow(() -> new BizException(ErrorCode.TOOL_NOT_FOUND));
        ResourceFile next = null;
        for (ResourceFile candidate : remaining) {
            if (Integer.valueOf(ResourceFile.OBJECT_STATUS_READY).equals(candidate.getObjectStatus())) {
                next = candidate;
                break;
            }
        }
        if (next == null) {
            tool.setLatestVersion(null);
            tools.saveAndFlush(tool);
            return;
        }
        markLatest(next);
    }

    private ResourceFile requireFile(Integer id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return resourceFiles.findById(id).orElseThrow(() -> new BizException(ErrorCode.FILE_NOT_FOUND));
    }

    private static String requireDisplayName(String displayName) {
        String trimmed = displayName.trim();
        if (trimmed.isEmpty() || trimmed.length() > DISPLAY_NAME_MAX) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return trimmed;
    }

    private static String requireVersion(String version) {
        String trimmed = version.trim();
        if (trimmed.isEmpty() || trimmed.length() > VERSION_MAX) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return trimmed;
    }

    private static String normalizePlatform(String platform) {
        if (!StringUtils.hasText(platform)) {
            return null;
        }
        String trimmed = platform.trim();
        if (!PLATFORMS.contains(trimmed)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return trimmed;
    }

    private static ResourceFileAdminDto toDto(ResourceFile file) {
        boolean latest = Integer.valueOf(ResourceFile.LATEST).equals(file.getLatest());
        return new ResourceFileAdminDto(file.getId(), file.getVersion(), file.getDisplayName(), file.getObjectKey(),
                file.getExt(), file.getFileSize(), file.getSha256(), file.getPlatform(), Boolean.valueOf(latest),
                file.getDownloadCount(), file.getObjectStatus(), EntityTimestamps.toOffset(file.getCreatedAt()));
    }
}

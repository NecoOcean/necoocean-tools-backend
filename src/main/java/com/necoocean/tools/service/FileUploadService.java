package com.necoocean.tools.service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.config.CosProperties;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.entity.ResourceFile;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.repository.ResourceFileRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import com.necoocean.tools.dto.admin.ResourceFileAdminDto;
import com.necoocean.tools.dto.admin.UploadCompleteRequest;
import com.necoocean.tools.dto.admin.UploadTicketDto;
import com.necoocean.tools.dto.admin.UploadTicketRequest;
import com.necoocean.tools.service.cos.CosObjectStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 两阶段直传。C-28 签发凭证并落占位，C-29 核对象存在后登记。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    private static final long MAX_FILE_BYTES = 209_715_200L;

    private static final int VERSION_MAX = 32;

    private static final int DISPLAY_NAME_MAX = 255;

    private static final int EXT_MAX = 16;

    private static final String UPLOAD_METHOD = "PUT";

    private static final String CONTENT_TYPE = "application/octet-stream";

    private static final String EXT_DOT = ".";

    private static final Pattern SHA256_PATTERN = Pattern.compile("^[0-9a-f]{64}$");

    private static final Set<String> ALLOWED_EXT = Set.of("apk", "exe", "msi", "dmg", "zip", "7z", "rar", "py",
            "tar.gz", "pdf", "txt", "md", "json", "yaml", "ini", "cfg", "whl", "AppImage", "deb");

    private static final Set<String> PLATFORMS = Set.of("Windows", "Android", "Mac", "通用");

    private final ResourceFileRepository resourceFiles;

    private final ToolRepository tools;

    private final CosObjectStore cosObjectStore;

    private final CosProperties cosProperties;

    private final ResourceFileStatusService resourceFileStatusService;

    /**
     * @param resourceFiles              资源文件
     * @param tools                      工具
     * @param cosObjectStore             对象存储
     * @param cosProperties              COS 配置
     * @param resourceFileStatusService  状态短事务
     */
    public FileUploadService(ResourceFileRepository resourceFiles, ToolRepository tools, CosObjectStore cosObjectStore,
            CosProperties cosProperties, ResourceFileStatusService resourceFileStatusService) {
        this.resourceFiles = resourceFiles;
        this.tools = tools;
        this.cosObjectStore = cosObjectStore;
        this.cosProperties = cosProperties;
        this.resourceFileStatusService = resourceFileStatusService;
    }

    /**
     * 申请预签名上传凭证并写入占位行。
     *
     * @param toolId  工具主键
     * @param request 请求体
     * @return 凭证
     */
    @Transactional(rollbackFor = Exception.class)
    public UploadTicketDto createUploadTicket(Integer toolId, UploadTicketRequest request) {
        if (!cosObjectStore.available()) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
        Tool tool = requireTool(toolId);
        if (request == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String displayName = requireDisplayName(request.getDisplayName());
        String ext = normalizeExt(request.getExt());
        long fileSize = requireFileSize(request.getFileSize());
        String version = requireVersion(request.getVersion());
        String platform = normalizePlatform(request.getPlatform());

        String objectKey = tool.getId() + "/" + version + "/" + UUID.randomUUID() + "." + ext;
        ResourceFile pending = new ResourceFile();
        pending.setTool(tool);
        pending.setVersion(version);
        pending.setDisplayName(displayName);
        pending.setObjectKey(objectKey);
        pending.setExt(ext);
        pending.setFileSize(Long.valueOf(fileSize));
        pending.setSha256(ResourceFile.PENDING_SHA256);
        pending.setPlatform(platform);
        pending.setLatest(Integer.valueOf(ResourceFile.NOT_LATEST));
        pending.setDownloadCount(Integer.valueOf(ResourceFile.DEFAULT_DOWNLOAD_COUNT));
        pending.setObjectStatus(Integer.valueOf(ResourceFile.OBJECT_STATUS_PENDING));
        resourceFiles.saveAndFlush(pending);

        String uploadUrl = cosObjectStore.generateUploadUrl(objectKey);
        OffsetDateTime expiresAt = EntityTimestamps.toOffset(
                EntityTimestamps.now().plusMinutes(cosProperties.getUploadTtlMinutes()));
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("Content-Type", CONTENT_TYPE);
        logger.info("upload ticket issued, fileId={}, toolId={}, key={}", pending.getId(), toolId, objectKey);
        return new UploadTicketDto(pending.getId(), objectKey, uploadUrl, UPLOAD_METHOD, headers, expiresAt);
    }

    /**
     * 直传完成后登记。方案 A：信任前端 sha256，只做格式校验。
     *
     * @param fileId  占位主键
     * @param request 请求体
     * @return 登记后的元信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ResourceFileAdminDto completeUpload(Integer fileId, UploadCompleteRequest request) {
        ResourceFile file = requireFile(fileId);
        if (!Integer.valueOf(ResourceFile.OBJECT_STATUS_PENDING).equals(file.getObjectStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (request == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String sha256 = requireSha256(request.getSha256());
        long fileSize = requireFileSize(request.getFileSize());
        if (!cosObjectStore.objectExists(file.getObjectKey())) {
            resourceFileStatusService.markAbnormal(file.getId());
            throw new BizException(ErrorCode.OBJECT_MISSING);
        }
        file.setSha256(sha256);
        file.setFileSize(Long.valueOf(fileSize));
        file.setObjectStatus(Integer.valueOf(ResourceFile.OBJECT_STATUS_READY));
        file.setCreatedAt(EntityTimestamps.now());
        resourceFiles.saveAndFlush(file);
        maybeMarkLatest(file);
        ResourceFile refreshed = requireFile(fileId);
        logger.info("upload completed, fileId={}, key={}", fileId, refreshed.getObjectKey());
        return toDto(refreshed);
    }

    private void maybeMarkLatest(ResourceFile file) {
        Integer toolId = file.getTool().getId();
        boolean hasLatest = resourceFiles
                .findByToolIdAndLatest(toolId, Integer.valueOf(ResourceFile.LATEST))
                .filter(existing -> Integer.valueOf(ResourceFile.OBJECT_STATUS_READY)
                        .equals(existing.getObjectStatus()))
                .isPresent();
        if (hasLatest) {
            return;
        }
        resourceFiles.clearLatestByToolId(toolId, Integer.valueOf(ResourceFile.NOT_LATEST));
        ResourceFile target = requireFile(file.getId());
        target.setLatest(Integer.valueOf(ResourceFile.LATEST));
        resourceFiles.saveAndFlush(target);
        Tool tool = tools.findById(toolId).orElseThrow(() -> new BizException(ErrorCode.TOOL_NOT_FOUND));
        tool.setLatestVersion(target.getVersion());
        tools.saveAndFlush(tool);
    }

    private Tool requireTool(Integer toolId) {
        if (toolId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return tools.findById(toolId).orElseThrow(() -> new BizException(ErrorCode.TOOL_NOT_FOUND));
    }

    private ResourceFile requireFile(Integer fileId) {
        if (fileId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return resourceFiles.findById(fileId).orElseThrow(() -> new BizException(ErrorCode.FILE_NOT_FOUND));
    }

    private static String requireDisplayName(String displayName) {
        if (!StringUtils.hasText(displayName)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String trimmed = displayName.trim();
        if (trimmed.length() > DISPLAY_NAME_MAX) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return trimmed;
    }

    private static String requireVersion(String version) {
        if (!StringUtils.hasText(version)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String trimmed = version.trim();
        if (trimmed.length() > VERSION_MAX) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return trimmed;
    }

    private static long requireFileSize(Long fileSize) {
        if (fileSize == null || fileSize.longValue() <= 0L) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (fileSize.longValue() > MAX_FILE_BYTES) {
            throw new BizException(ErrorCode.FILE_TOO_LARGE);
        }
        return fileSize.longValue();
    }

    private static String requireSha256(String sha256) {
        if (!StringUtils.hasText(sha256)) {
            throw new BizException(ErrorCode.SHA256_INVALID);
        }
        String trimmed = sha256.trim().toLowerCase(Locale.ROOT);
        if (!SHA256_PATTERN.matcher(trimmed).matches()) {
            throw new BizException(ErrorCode.SHA256_INVALID);
        }
        return trimmed;
    }

    private static String normalizeExt(String ext) {
        if (!StringUtils.hasText(ext)) {
            throw new BizException(ErrorCode.FILE_TYPE_REJECTED);
        }
        String trimmed = ext.trim();
        if (trimmed.startsWith(EXT_DOT)) {
            trimmed = trimmed.substring(1);
        }
        String matched = null;
        for (String allowed : ALLOWED_EXT) {
            if (allowed.equalsIgnoreCase(trimmed)) {
                matched = allowed;
                break;
            }
        }
        if (matched == null || matched.length() > EXT_MAX) {
            throw new BizException(ErrorCode.FILE_TYPE_REJECTED);
        }
        return matched;
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

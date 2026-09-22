package com.necoocean.tools.dto.admin;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * C-28 上传凭证响应。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class UploadTicketDto {

    private final Integer fileId;

    private final String objectKey;

    private final String uploadUrl;

    private final String uploadMethod;

    private final Map<String, String> uploadHeaders;

    private final OffsetDateTime expiresAt;

    /**
     * @param fileId        占位主键
     * @param objectKey     对象键
     * @param uploadUrl     预签名 PUT
     * @param uploadMethod  固定 PUT
     * @param uploadHeaders 上传须带请求头
     * @param expiresAt     过期时间
     */
    public UploadTicketDto(Integer fileId, String objectKey, String uploadUrl, String uploadMethod,
            Map<String, String> uploadHeaders, OffsetDateTime expiresAt) {
        this.fileId = fileId;
        this.objectKey = objectKey;
        this.uploadUrl = uploadUrl;
        this.uploadMethod = uploadMethod;
        this.uploadHeaders = uploadHeaders == null ? Map.of() : Map.copyOf(uploadHeaders);
        this.expiresAt = expiresAt;
    }

    /**
     * @return 占位主键
     */
    public Integer getFileId() {
        return fileId;
    }

    /**
     * @return 对象键
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * @return 预签名 URL
     */
    public String getUploadUrl() {
        return uploadUrl;
    }

    /**
     * @return PUT
     */
    public String getUploadMethod() {
        return uploadMethod;
    }

    /**
     * @return 请求头
     */
    public Map<String, String> getUploadHeaders() {
        return uploadHeaders;
    }

    /**
     * @return 过期时间
     */
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }
}

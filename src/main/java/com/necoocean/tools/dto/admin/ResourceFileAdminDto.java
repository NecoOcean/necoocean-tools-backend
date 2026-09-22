package com.necoocean.tools.dto.admin;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 后台文件元信息。含 object_key，供管理端对照。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class ResourceFileAdminDto {

    private final Integer id;
    private final String version;
    private final String displayName;
    private final String objectKey;
    private final String ext;
    private final Long fileSize;
    private final String sha256;
    private final String platform;
    private final Boolean latest;
    private final Integer downloadCount;
    private final OffsetDateTime createdAt;

    /**
     * @param id            主键
     * @param version       版本
     * @param displayName   文件名
     * @param objectKey     对象键
     * @param ext           扩展名
     * @param fileSize      字节
     * @param sha256        校验值
     * @param platform      平台
     * @param latest        是否推荐
     * @param downloadCount 下载次数
     * @param createdAt     上传时间
     */
    public ResourceFileAdminDto(Integer id, String version, String displayName, String objectKey, String ext,
            Long fileSize, String sha256, String platform, Boolean latest, Integer downloadCount,
            OffsetDateTime createdAt) {
        this.id = id;
        this.version = version;
        this.displayName = displayName;
        this.objectKey = objectKey;
        this.ext = ext;
        this.fileSize = fileSize;
        this.sha256 = sha256;
        this.platform = platform;
        this.latest = latest;
        this.downloadCount = downloadCount;
        this.createdAt = createdAt;
    }

    /**
     * 读取字段。
     *
     * @return 值
     */
    public Integer getId() {
        return id;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getVersion() {
        return version;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getDisplayName() {
        return displayName;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getObjectKey() {
        return objectKey;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getExt() {
        return ext;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public Long getFileSize() {
        return fileSize;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getSha256() {
        return sha256;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getPlatform() {
        return platform;
    }
    /**
     * 是否当前推荐版本。
     *
     * @return true 表示推荐
     */
    @JsonProperty("is_latest")
    public Boolean getLatest() {
        return latest;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public Integer getDownloadCount() {
        return downloadCount;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "ResourceFileAdminDto{id=" + id + '}';
    }
}
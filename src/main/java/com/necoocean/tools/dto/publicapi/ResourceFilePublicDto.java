package com.necoocean.tools.dto.publicapi;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 可下载文件。只给出站内下载路径，不给出对象键。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class ResourceFilePublicDto {

    private final Integer id;

    private final String version;

    private final String displayName;

    private final Long fileSize;

    private final String sha256;

    private final String platform;

    private final Boolean latest;

    private final String downloadUrl;

    private final OffsetDateTime createdAt;

    /**
     * @param id          文件主键
     * @param version     版本号
     * @param displayName 展示文件名
     * @param fileSize    字节数
     * @param sha256      校验值
     * @param platform    平台，可以为 null
     * @param latest      是否推荐版本
     * @param downloadUrl 站内下载路径
     * @param createdAt   上传时间
     */
    public ResourceFilePublicDto(Integer id, String version, String displayName, Long fileSize, String sha256,
            String platform, Boolean latest, String downloadUrl, OffsetDateTime createdAt) {
        this.id = id;
        this.version = version;
        this.displayName = displayName;
        this.fileSize = fileSize;
        this.sha256 = sha256;
        this.platform = platform;
        this.latest = latest;
        this.downloadUrl = downloadUrl;
        this.createdAt = createdAt;
    }

    /**
     * 文件主键。
     *
     * @return 主键
     */
    public Integer getId() {
        return id;
    }

    /**
     * 版本号。
     *
     * @return 版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 展示文件名。
     *
     * @return 文件名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 文件大小。
     *
     * @return 字节数
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * SHA-256。
     *
     * @return 64 位十六进制
     */
    public String getSha256() {
        return sha256;
    }

    /**
     * 适用平台。
     *
     * @return 平台，可以为 null
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
     * 站内下载路径。
     *
     * @return 以 /download/ 开头的路径
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * 上传时间。
     *
     * @return 带 +08:00 的时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 只输出主键，不输出校验值。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "ResourceFilePublicDto{id=" + id + '}';
    }
}

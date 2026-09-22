package com.necoocean.tools.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 编辑文件元信息。不可改 object_key。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class ResourceFileUpdateRequest {

    private String displayName;

    private String platform;

    private String version;

    private Boolean latest;

    private boolean latestPresent;

    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getDisplayName() {
        return displayName;
    }
    /**
     * 写入字段。
     *
     * @param displayName 值
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
     * 写入字段。
     *
     * @param platform 值
     */
    public void setPlatform(String platform) {
        this.platform = platform;
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
     * 写入字段。
     *
     * @param version 值
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 是否推荐版本。
     *
     * @return true 表示推荐
     */
    @JsonProperty("is_latest")
    public Boolean getLatest() {
        return latest;
    }

    /**
     * 写入是否推荐。即使为 null 也视为显式传入。
     *
     * @param latest true 或 false，可以为 null
     */
    @JsonProperty("is_latest")
    public void setLatest(Boolean latest) {
        this.latestPresent = true;
        this.latest = latest;
    }

    /**
     * 请求体是否包含 is_latest。
     *
     * @return 包含时为 true
     */
    public boolean isLatestPresent() {
        return latestPresent;
    }
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "ResourceFileUpdateRequest{displayName=" + displayName + '}';
    }
}
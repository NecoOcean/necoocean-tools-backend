package com.necoocean.tools.dto.admin;

import java.time.OffsetDateTime;

/**
 * 更新日志新增与编辑请求。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class ReleaseNoteWriteRequest {

    private String version;

    private String content;

    private OffsetDateTime releasedAt;

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
     * 读取字段。
     *
     * @return 值
     */
    public String getContent() {
        return content;
    }
    /**
     * 写入字段。
     *
     * @param content 值
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 读取字段。
     *
     * @return 值
     */
    public OffsetDateTime getReleasedAt() {
        return releasedAt;
    }
    /**
     * 写入字段。
     *
     * @param releasedAt 值
     */
    public void setReleasedAt(OffsetDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "ReleaseNoteWriteRequest{version=" + version + '}';
    }
}
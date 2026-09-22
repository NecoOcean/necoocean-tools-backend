package com.necoocean.tools.dto.admin;

import java.time.OffsetDateTime;

/**
 * 后台更新日志。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class ReleaseNoteAdminDto {

    private final Integer id;
    private final String version;
    private final String content;
    private final OffsetDateTime releasedAt;

    /**
     * @param id         主键
     * @param version    版本
     * @param content    正文
     * @param releasedAt 发布时间
     */
    public ReleaseNoteAdminDto(Integer id, String version, String content, OffsetDateTime releasedAt) {
        this.id = id;
        this.version = version;
        this.content = content;
        this.releasedAt = releasedAt;
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
    public String getContent() {
        return content;
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
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "ReleaseNoteAdminDto{id=" + id + '}';
    }
}
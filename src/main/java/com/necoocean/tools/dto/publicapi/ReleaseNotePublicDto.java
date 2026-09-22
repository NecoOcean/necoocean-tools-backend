package com.necoocean.tools.dto.publicapi;

import java.time.OffsetDateTime;

/**
 * 一条更新日志。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class ReleaseNotePublicDto {

    private final Integer id;

    private final String version;

    private final String content;

    private final OffsetDateTime releasedAt;

    /**
     * @param id         日志主键
     * @param version    版本号
     * @param content    更新说明
     * @param releasedAt 发布时间
     */
    public ReleaseNotePublicDto(Integer id, String version, String content, OffsetDateTime releasedAt) {
        this.id = id;
        this.version = version;
        this.content = content;
        this.releasedAt = releasedAt;
    }

    /**
     * 日志主键。
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
     * 更新说明。
     *
     * @return Markdown
     */
    public String getContent() {
        return content;
    }

    /**
     * 发布时间。
     *
     * @return 带 +08:00 的时间
     */
    public OffsetDateTime getReleasedAt() {
        return releasedAt;
    }

    /**
     * 只输出主键和版本。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "ReleaseNotePublicDto{id=" + id + ", version=" + version + '}';
    }
}

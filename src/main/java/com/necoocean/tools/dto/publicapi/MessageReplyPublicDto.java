package com.necoocean.tools.dto.publicapi;

import java.time.OffsetDateTime;

/**
 * 公开的管理员回复。不含内部处理状态。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageReplyPublicDto {

    private final Integer id;

    private final String content;

    private final OffsetDateTime createdAt;

    /**
     * @param id        回复主键
     * @param content   回复正文
     * @param createdAt 回复时间
     */
    public MessageReplyPublicDto(Integer id, String content, OffsetDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
    }

    /**
     * 回复主键。
     *
     * @return 主键
     */
    public Integer getId() {
        return id;
    }

    /**
     * 回复正文。
     *
     * @return 正文
     */
    public String getContent() {
        return content;
    }

    /**
     * 回复时间。
     *
     * @return 带 +08:00 的时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 只输出主键。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "MessageReplyPublicDto{id=" + id + '}';
    }
}

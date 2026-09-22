package com.necoocean.tools.dto.publicapi;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 已通过审核的公开留言。不含邮箱、IP 哈希、处理状态和审核状态。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessagePublicDto {

    private final Integer id;

    private final String nickname;

    private final Integer category;

    private final String categoryText;

    private final String content;

    private final String osPlatform;

    private final String toolVersion;

    private final String reproSteps;

    private final Boolean pinned;

    private final OffsetDateTime createdAt;

    private final List<MessageReplyPublicDto> replies;

    /**
     * @param id           留言主键
     * @param nickname     昵称
     * @param category     分类枚举
     * @param categoryText 分类文本
     * @param content      正文
     * @param osPlatform   系统，非 BUG 时为 null
     * @param toolVersion  版本，非 BUG 时为 null
     * @param reproSteps   复现步骤，非 BUG 时为 null
     * @param pinned       是否置顶
     * @param createdAt    提交时间
     * @param replies      回复，没有时为空列表
     */
    public MessagePublicDto(Integer id, String nickname, Integer category, String categoryText, String content,
            String osPlatform, String toolVersion, String reproSteps, Boolean pinned, OffsetDateTime createdAt,
            List<MessageReplyPublicDto> replies) {
        this.id = id;
        this.nickname = nickname;
        this.category = category;
        this.categoryText = categoryText;
        this.content = content;
        this.osPlatform = osPlatform;
        this.toolVersion = toolVersion;
        this.reproSteps = reproSteps;
        this.pinned = pinned;
        this.createdAt = createdAt;
        this.replies = replies;
    }

    /**
     * 留言主键。
     *
     * @return 主键
     */
    public Integer getId() {
        return id;
    }

    /**
     * 昵称。
     *
     * @return 昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 留言分类枚举。
     *
     * @return 1 到 4
     */
    public Integer getCategory() {
        return category;
    }

    /**
     * 留言分类文本。
     *
     * @return 展示文案
     */
    public String getCategoryText() {
        return categoryText;
    }

    /**
     * 留言正文。
     *
     * @return 正文
     */
    public String getContent() {
        return content;
    }

    /**
     * 操作系统。只有 BUG 类留言才返回。
     *
     * @return 系统，否则为 null
     */
    public String getOsPlatform() {
        return osPlatform;
    }

    /**
     * 工具版本。只有 BUG 类留言才返回。
     *
     * @return 版本，否则为 null
     */
    public String getToolVersion() {
        return toolVersion;
    }

    /**
     * 复现步骤。只有 BUG 类留言才返回。
     *
     * @return 步骤，否则为 null
     */
    public String getReproSteps() {
        return reproSteps;
    }

    /**
     * 是否置顶。
     *
     * @return true 表示置顶
     */
    @JsonProperty("is_pinned")
    public Boolean getPinned() {
        return pinned;
    }

    /**
     * 提交时间。
     *
     * @return 带 +08:00 的时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 管理员回复。
     *
     * @return 回复列表
     */
    public List<MessageReplyPublicDto> getReplies() {
        return replies;
    }

    /**
     * 只输出主键，不输出正文。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "MessagePublicDto{id=" + id + '}';
    }
}

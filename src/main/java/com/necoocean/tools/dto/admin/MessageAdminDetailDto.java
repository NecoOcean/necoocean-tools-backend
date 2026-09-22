package com.necoocean.tools.dto.admin;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.necoocean.tools.dto.publicapi.CategoryDto;
import com.necoocean.tools.dto.publicapi.MessageReplyPublicDto;

/**
 * 后台留言详情。唯一返回 contact_email 的接口形状。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageAdminDetailDto {

    private final Integer id;
    private final CategoryDto tool;
    private final String nickname;
    private final String contactEmail;
    private final Integer category;
    private final String categoryText;
    private final String content;
    private final String osPlatform;
    private final String toolVersion;
    private final String reproSteps;
    private final Integer status;
    private final String statusText;
    private final Integer auditStatus;
    private final String auditStatusText;
    private final Boolean keywordFlagged;
    private final Boolean pinned;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime auditedAt;
    private final List<MessageReplyPublicDto> replies;

    /**
     * @param id              主键
     * @param tool            工具
     * @param nickname        昵称
     * @param contactEmail    邮箱，未填时为 null
     * @param category        分类
     * @param categoryText    分类文本
     * @param content         正文
     * @param osPlatform      系统
     * @param toolVersion     版本
     * @param reproSteps      复现步骤
     * @param status          处理状态
     * @param statusText      处理状态文本
     * @param auditStatus     审核状态
     * @param auditStatusText 审核状态文本
     * @param keywordFlagged  关键词命中
     * @param pinned          置顶
     * @param createdAt       提交时间
     * @param auditedAt       审核时间
     * @param replies         回复
     */
    public MessageAdminDetailDto(Integer id, CategoryDto tool, String nickname, String contactEmail, Integer category,
            String categoryText, String content, String osPlatform, String toolVersion, String reproSteps,
            Integer status, String statusText, Integer auditStatus, String auditStatusText, Boolean keywordFlagged,
            Boolean pinned, OffsetDateTime createdAt, OffsetDateTime auditedAt, List<MessageReplyPublicDto> replies) {
        this.id = id;
        this.tool = tool;
        this.nickname = nickname;
        this.contactEmail = contactEmail;
        this.category = category;
        this.categoryText = categoryText;
        this.content = content;
        this.osPlatform = osPlatform;
        this.toolVersion = toolVersion;
        this.reproSteps = reproSteps;
        this.status = status;
        this.statusText = statusText;
        this.auditStatus = auditStatus;
        this.auditStatusText = auditStatusText;
        this.keywordFlagged = keywordFlagged;
        this.pinned = pinned;
        this.createdAt = createdAt;
        this.auditedAt = auditedAt;
        this.replies = replies == null ? List.of() : List.copyOf(replies);
    }

    /** @return 主键 */
    public Integer getId() {
        return id;
    }
    /** @return 工具 */
    public CategoryDto getTool() {
        return tool;
    }
    /** @return 昵称 */
    public String getNickname() {
        return nickname;
    }
    /** @return 邮箱 */
    public String getContactEmail() {
        return contactEmail;
    }
    /** @return 分类 */
    public Integer getCategory() {
        return category;
    }
    /** @return 分类文本 */
    public String getCategoryText() {
        return categoryText;
    }
    /** @return 正文 */
    public String getContent() {
        return content;
    }
    /** @return 系统 */
    public String getOsPlatform() {
        return osPlatform;
    }
    /** @return 版本 */
    public String getToolVersion() {
        return toolVersion;
    }
    /** @return 复现步骤 */
    public String getReproSteps() {
        return reproSteps;
    }
    /** @return 处理状态 */
    public Integer getStatus() {
        return status;
    }
    /** @return 处理状态文本 */
    public String getStatusText() {
        return statusText;
    }
    /** @return 审核状态 */
    public Integer getAuditStatus() {
        return auditStatus;
    }
    /** @return 审核状态文本 */
    public String getAuditStatusText() {
        return auditStatusText;
    }
    /** @return 关键词命中 */
    public Boolean getKeywordFlagged() {
        return keywordFlagged;
    }
    /** @return 是否置顶 */
    @JsonProperty("is_pinned")
    public Boolean getPinned() {
        return pinned;
    }
    /** @return 提交时间 */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    /** @return 审核时间 */
    public OffsetDateTime getAuditedAt() {
        return auditedAt;
    }
    /** @return 回复 */
    public List<MessageReplyPublicDto> getReplies() {
        return replies;
    }
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "MessageAdminDetailDto{id=" + id + '}';
    }
}
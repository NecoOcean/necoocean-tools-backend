package com.necoocean.tools.dto.admin;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.necoocean.tools.dto.publicapi.CategoryDto;

/**
 * 后台留言列表项。不含 contact_email，只给 has_email。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageAdminListDto {

    private final Integer id;
    private final CategoryDto tool;
    private final String nickname;
    private final Integer category;
    private final String categoryText;
    private final String content;
    private final Integer status;
    private final String statusText;
    private final Integer auditStatus;
    private final String auditStatusText;
    private final Boolean keywordFlagged;
    private final Boolean hasEmail;
    private final Boolean pinned;
    private final OffsetDateTime createdAt;

    /**
     * @param id              主键
     * @param tool            所属工具（复用 id/name 形状）
     * @param nickname        昵称
     * @param category        留言分类
     * @param categoryText    分类文本
     * @param content         正文
     * @param status          处理状态
     * @param statusText      处理状态文本
     * @param auditStatus     审核状态
     * @param auditStatusText 审核状态文本
     * @param keywordFlagged  是否命中关键词
     * @param hasEmail        是否有邮箱
     * @param pinned          是否置顶
     * @param createdAt       提交时间
     */
    public MessageAdminListDto(Integer id, CategoryDto tool, String nickname, Integer category, String categoryText,
            String content, Integer status, String statusText, Integer auditStatus, String auditStatusText,
            Boolean keywordFlagged, Boolean hasEmail, Boolean pinned, OffsetDateTime createdAt) {
        this.id = id;
        this.tool = tool;
        this.nickname = nickname;
        this.category = category;
        this.categoryText = categoryText;
        this.content = content;
        this.status = status;
        this.statusText = statusText;
        this.auditStatus = auditStatus;
        this.auditStatusText = auditStatusText;
        this.keywordFlagged = keywordFlagged;
        this.hasEmail = hasEmail;
        this.pinned = pinned;
        this.createdAt = createdAt;
    }

    /** @return 主键 */
    public Integer getId() {
        return id;
    }
    /** @return 所属工具 */
    public CategoryDto getTool() {
        return tool;
    }
    /** @return 昵称 */
    public String getNickname() {
        return nickname;
    }
    /** @return 留言分类 */
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
    /** @return 是否命中关键词 */
    public Boolean getKeywordFlagged() {
        return keywordFlagged;
    }
    /** @return 是否有邮箱 */
    public Boolean getHasEmail() {
        return hasEmail;
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
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "MessageAdminListDto{id=" + id + '}';
    }
}
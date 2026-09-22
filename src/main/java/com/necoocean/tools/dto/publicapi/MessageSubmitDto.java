package com.necoocean.tools.dto.publicapi;

import java.time.OffsetDateTime;

/**
 * 留言提交结果。不回传正文和邮箱，前端提交成功后重新拉取列表。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageSubmitDto {

    private final Integer id;

    private final Integer auditStatus;

    private final String auditStatusText;

    private final Boolean keywordFlagged;

    private final OffsetDateTime createdAt;

    /**
     * @param id              留言主键
     * @param auditStatus     审核状态
     * @param auditStatusText 审核状态文本
     * @param keywordFlagged  是否命中关键词
     * @param createdAt       提交时间
     */
    public MessageSubmitDto(Integer id, Integer auditStatus, String auditStatusText, Boolean keywordFlagged,
            OffsetDateTime createdAt) {
        this.id = id;
        this.auditStatus = auditStatus;
        this.auditStatusText = auditStatusText;
        this.keywordFlagged = keywordFlagged;
        this.createdAt = createdAt;
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
     * 审核状态。
     *
     * @return 1 待审核，2 已通过，3 未通过
     */
    public Integer getAuditStatus() {
        return auditStatus;
    }

    /**
     * 审核状态文本。
     *
     * @return 文本
     */
    public String getAuditStatusText() {
        return auditStatusText;
    }

    /**
     * 是否命中关键词。命中仍然公开。
     *
     * @return 命中时为 true
     */
    public Boolean getKeywordFlagged() {
        return keywordFlagged;
    }

    /**
     * 提交时间。
     *
     * @return 带偏移的时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

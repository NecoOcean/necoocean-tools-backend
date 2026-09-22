package com.necoocean.tools.dto.publicapi;

import java.time.OffsetDateTime;

/**
 * 留言状态查询。只含状态，不含正文和邮箱，避免自增主键被枚举后泄露内容。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageStatusDto {

    private final Integer id;

    private final Integer auditStatus;

    private final String auditStatusText;

    private final OffsetDateTime createdAt;

    /**
     * @param id              留言主键
     * @param auditStatus     审核状态
     * @param auditStatusText 审核状态文本
     * @param createdAt       提交时间
     */
    public MessageStatusDto(Integer id, Integer auditStatus, String auditStatusText, OffsetDateTime createdAt) {
        this.id = id;
        this.auditStatus = auditStatus;
        this.auditStatusText = auditStatusText;
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
     * 提交时间。
     *
     * @return 带偏移的时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

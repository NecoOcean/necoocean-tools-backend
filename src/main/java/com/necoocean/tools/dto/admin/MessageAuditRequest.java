package com.necoocean.tools.dto.admin;

/**
 * 单条审核请求。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageAuditRequest {

    private Integer auditStatus;

    /**
     * 审核结果。
     *
     * @return 2 通过，3 未通过
     */
    public Integer getAuditStatus() {
        return auditStatus;
    }
    /**
     * 写入审核结果。
     *
     * @param auditStatus 2 或 3
     */
    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "MessageAuditRequest{auditStatus=" + auditStatus + '}';
    }
}
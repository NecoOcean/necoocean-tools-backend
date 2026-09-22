package com.necoocean.tools.dto.admin;

import java.util.List;

/**
 * 批量审核请求。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageAuditBatchRequest {

    private List<Integer> ids;

    private Integer auditStatus;

    /**
     * 留言主键列表。
     *
     * @return 主键列表
     */
    public List<Integer> getIds() {
        return ids;
    }
    /**
     * 写入留言主键列表。
     *
     * @param ids 主键列表
     */
    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

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
        return "MessageAuditBatchRequest{size=" + (ids == null ? 0 : ids.size()) + '}';
    }
}
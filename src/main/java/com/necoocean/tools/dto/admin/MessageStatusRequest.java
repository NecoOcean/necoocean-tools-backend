package com.necoocean.tools.dto.admin;

/**
 * 留言处理状态请求。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageStatusRequest {

    private Integer status;

    /**
     * 处理状态。
     *
     * @return 1 到 4
     */
    public Integer getStatus() {
        return status;
    }
    /**
     * 写入处理状态。
     *
     * @param status 1 到 4
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "MessageStatusRequest{status=" + status + '}';
    }
}
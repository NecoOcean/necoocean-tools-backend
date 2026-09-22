package com.necoocean.tools.dto.admin;

/**
 * 上下架请求。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class ToolStatusRequest {

    private Integer status;

    /**
     * 上下架状态。
     *
     * @return 1 上架，0 下架
     */
    public Integer getStatus() {
        return status;
    }
    /**
     * 写入上下架状态。
     *
     * @param status 1 或 0
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
        return "ToolStatusRequest{status=" + status + '}';
    }
}
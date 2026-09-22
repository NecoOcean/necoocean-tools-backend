package com.necoocean.tools.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 留言置顶请求。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessagePinRequest {

    private Boolean pinned;

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
     * 写入是否置顶。
     *
     * @param pinned true 或 false
     */
    @JsonProperty("is_pinned")
    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "MessagePinRequest{pinned=" + pinned + '}';
    }
}
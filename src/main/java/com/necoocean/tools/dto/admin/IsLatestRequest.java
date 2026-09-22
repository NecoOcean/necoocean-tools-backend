package com.necoocean.tools.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 设为推荐版本请求。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class IsLatestRequest {

    private Boolean latest;

    /**
     * 是否推荐版本。
     *
     * @return true 表示设为推荐
     */
    @JsonProperty("is_latest")
    public Boolean getLatest() {
        return latest;
    }
    /**
     * 写入是否推荐版本。
     *
     * @param latest true 或 false
     */
    @JsonProperty("is_latest")
    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "IsLatestRequest{latest=" + latest + '}';
    }
}
package com.necoocean.tools.dto.admin;

import java.time.OffsetDateTime;

/**
 * 当前登录态。不包含口令哈希和失败次数。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class AdminSessionDto {

    private final String username;

    private final OffsetDateTime lastLoginAt;

    /**
     * @param username    登录账号
     * @param lastLoginAt 最后登录时间，可以为 null
     */
    public AdminSessionDto(String username, OffsetDateTime lastLoginAt) {
        this.username = username;
        this.lastLoginAt = lastLoginAt;
    }

    /**
     * 登录账号。
     *
     * @return 账号
     */
    public String getUsername() {
        return username;
    }
    /**
     * 最后登录时间。
     *
     * @return 带 +08:00 的时间，从未登录时为 null
     */
    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    /**
     * 只输出账号。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "AdminSessionDto{username=" + username + '}';
    }
}

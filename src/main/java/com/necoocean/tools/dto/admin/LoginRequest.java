package com.necoocean.tools.dto.admin;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求。toString 不输出口令。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    /**
     * 登录账号。
     *
     * @return 账号
     */
    public String getUsername() {
        return username;
    }

    /**
     * 写入登录账号。
     *
     * @param username 账号
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 明文口令。只在本次请求内使用。
     *
     * @return 口令
     */
    public String getPassword() {
        return password;
    }

    /**
     * 写入明文口令。
     *
     * @param password 口令
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 只输出账号。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "LoginRequest{username=" + username + '}';
    }
}

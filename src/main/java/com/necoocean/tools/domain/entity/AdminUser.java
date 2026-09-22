package com.necoocean.tools.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * 管理员账号。与业务表无外键。口令只存哈希，toString 不输出哈希。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Entity
@Table(name = "admin_users")
public class AdminUser {

    /** 新账号的连续失败次数。 */
    public static final int DEFAULT_FAILED_ATTEMPTS = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts;

    @Column(name = "last_failed_at")
    private LocalDateTime lastFailedAt;

    @PrePersist
    private void fillDefaults() {
        if (failedAttempts == null) {
            failedAttempts = DEFAULT_FAILED_ATTEMPTS;
        }
    }

    /**
     * 主键。
     *
     * @return 主键，未持久化时为 null
     */
    public Integer getId() {
        return id;
    }

    /**
     * 写入主键。仅供持久化框架回填。
     *
     * @param id 主键
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 登录账号。一期只有一个。
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
     * 加盐口令哈希。不存明文。
     *
     * @return 哈希
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 写入口令哈希。
     *
     * @param passwordHash 哈希
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * 最后登录时间。
     *
     * @return 时间，从未登录时为 null
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * 写入最后登录时间。
     *
     * @param lastLoginAt 时间，可以为 null
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    /**
     * 连续登录失败次数。成功登录后清零。
     *
     * @return 次数
     */
    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * 写入连续失败次数。为空时在插入前记为 0。
     *
     * @param failedAttempts 次数，可以为 null
     */
    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    /**
     * 最近一次登录失败时间。用来计算 15 分钟锁定窗口。
     *
     * @return 失败时间，尚未失败时为 null
     */
    public LocalDateTime getLastFailedAt() {
        return lastFailedAt;
    }

    /**
     * 写入最近一次登录失败时间。
     *
     * @param lastFailedAt 失败时间，可以为 null
     */
    public void setLastFailedAt(LocalDateTime lastFailedAt) {
        this.lastFailedAt = lastFailedAt;
    }

    /**
     * 只输出主键和账号，不输出口令哈希。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "AdminUser{id=" + id + ", username=" + username + '}';
    }
}

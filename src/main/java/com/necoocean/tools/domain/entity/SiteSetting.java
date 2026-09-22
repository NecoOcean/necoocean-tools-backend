package com.necoocean.tools.domain.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 站点配置。一行一个键。未在白名单内的键由业务层拒绝写入。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Entity
@Table(name = "site_settings")
public class SiteSetting {

    /** 网站标题。 */
    public static final String SITE_NAME = "site_name";

    /** 首页个人简介。 */
    public static final String HOME_INTRO = "home_intro";

    /** 版权信息。 */
    public static final String COPYRIGHT = "copyright";

    /** ICP 备案号。空值时前台不展示。 */
    public static final String ICP_NUMBER = "icp_number";

    /** 首页公告。 */
    public static final String ANNOUNCEMENT = "announcement";

    /** 「关于我」页面内容。 */
    public static final String ABOUT_CONTENT = "about_content";

    /** 隐私政策内容。 */
    public static final String PRIVACY_CONTENT = "privacy_content";

    /** 留言板开关。 */
    public static final String MESSAGE_BOARD_ENABLED = "message_board_enabled";

    /** 审核模式。post 为先发后审，pre 为先审后发。 */
    public static final String MESSAGE_AUDIT_MODE = "message_audit_mode";

    /** 先发后审。一期默认。 */
    public static final String AUDIT_MODE_POST = "post";

    /** 先审后发。二期终态。 */
    public static final String AUDIT_MODE_PRE = "pre";

    /** 留言板开启时的配置值。 */
    public static final String BOARD_ON = "true";

    /** 留言板关闭时的配置值。 */
    public static final String BOARD_OFF = "false";

    private static final Set<String> DEFINED_KEYS = Set.of(
            SITE_NAME,
            HOME_INTRO,
            COPYRIGHT,
            ICP_NUMBER,
            ANNOUNCEMENT,
            ABOUT_CONTENT,
            PRIVACY_CONTENT,
            MESSAGE_BOARD_ENABLED,
            MESSAGE_AUDIT_MODE);

    @Id
    @Column(name = "key", nullable = false, length = 100)
    private String key;

    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    @Column(name = "value")
    private String value;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 判断键是否在一期白名单内。
     *
     * @param key 配置键，可以为 null
     * @return 在白名单内时为 true
     */
    public static boolean isDefinedKey(String key) {
        if (key == null) {
            return false;
        }
        return DEFINED_KEYS.contains(key);
    }

    @PrePersist
    private void fillUpdatedAt() {
        if (updatedAt == null) {
            updatedAt = EntityTimestamps.now();
        }
    }

    @PreUpdate
    private void touchUpdatedAt() {
        updatedAt = EntityTimestamps.now();
    }

    /**
     * 配置键。
     *
     * @return 键
     */
    public String getKey() {
        return key;
    }

    /**
     * 写入配置键。
     *
     * @param key 键
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 配置值。统一存字符串，空值用 null，不用空字符串。
     *
     * @return 值，未填写时为 null
     */
    public String getValue() {
        return value;
    }

    /**
     * 写入配置值。
     *
     * @param value 值，可以为 null
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 最后更新时间。
     *
     * @return 东八区墙钟时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 写入最后更新时间。为空时在插入前自动补上，更新时自动刷新。
     *
     * @param updatedAt 时间，可以为 null
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 只输出键，不输出配置正文。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "SiteSetting{key=" + key + '}';
    }
}

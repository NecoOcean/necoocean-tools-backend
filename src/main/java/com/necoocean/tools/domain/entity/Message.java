package com.necoocean.tools.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

/**
 * 访客留言。不作为公开接口响应；contactEmail 与 ipHash 不得进入公开 DTO。
 * 一期 auditStatus 默认已通过，先发后审。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Entity
@Table(name = "messages")
public class Message {

    /** 使用体验。 */
    public static final int CATEGORY_EXPERIENCE = 1;

    /** 功能建议。 */
    public static final int CATEGORY_SUGGESTION = 2;

    /** BUG 报错。 */
    public static final int CATEGORY_BUG = 3;

    /** 问题咨询。 */
    public static final int CATEGORY_QUESTION = 4;

    /** 未处理。 */
    public static final int STATUS_PENDING = 1;

    /** 已处理。 */
    public static final int STATUS_HANDLED = 2;

    /** 已跟进。 */
    public static final int STATUS_FOLLOWED = 3;

    /** 无需处理。 */
    public static final int STATUS_IGNORED = 4;

    /** 待审核。二期先审后发时作为新留言默认值。 */
    public static final int AUDIT_PENDING = 1;

    /** 已通过，公开可见。一期新留言的默认值。 */
    public static final int AUDIT_APPROVED = 2;

    /** 未通过，不公开。 */
    public static final int AUDIT_REJECTED = 3;

    /** 置顶。 */
    public static final int PINNED = 1;

    /** 不置顶。 */
    public static final int NOT_PINNED = 0;

    /** 命中关键词。只标记，不拦截。 */
    public static final int KEYWORD_FLAGGED = 1;

    /** 未命中关键词。 */
    public static final int NOT_KEYWORD_FLAGGED = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tool_id", nullable = false, foreignKey = @ForeignKey(name = "fk_messages_tool"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tool tool;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "contact_email", length = 254)
    private String contactEmail;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer category;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "os_platform", length = 50)
    private String osPlatform;

    @Column(name = "tool_version", length = 32)
    private String toolVersion;

    @Column(name = "repro_steps", length = 1000)
    private String reproSteps;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer status;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "is_pinned", nullable = false)
    private Integer pinned;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "audit_status", nullable = false)
    private Integer auditStatus;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "keyword_flagged", nullable = false)
    private Integer keywordFlagged;

    @Column(name = "audited_at")
    private LocalDateTime auditedAt;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ip_hash", nullable = false, length = 64)
    private String ipHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void fillDefaults() {
        if (createdAt == null) {
            createdAt = EntityTimestamps.now();
        }
        if (status == null) {
            status = STATUS_PENDING;
        }
        if (pinned == null) {
            pinned = NOT_PINNED;
        }
        if (auditStatus == null) {
            auditStatus = AUDIT_APPROVED;
        }
        if (keywordFlagged == null) {
            keywordFlagged = NOT_KEYWORD_FLAGGED;
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
     * 留言所属工具。
     *
     * @return 工具
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * 写入所属工具。
     *
     * @param tool 工具
     */
    public void setTool(Tool tool) {
        this.tool = tool;
    }

    /**
     * 昵称。访客可以填「匿名」。
     *
     * @return 昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 写入昵称。
     *
     * @param nickname 昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 选填邮箱。仅管理员可见，不建唯一索引。
     *
     * @return 邮箱，未填写时为 null
     */
    public String getContactEmail() {
        return contactEmail;
    }

    /**
     * 写入邮箱。
     *
     * @param contactEmail 邮箱，可以为 null
     */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /**
     * 留言分类。
     *
     * @return 1 使用体验，2 功能建议，3 BUG 报错，4 问题咨询
     */
    public Integer getCategory() {
        return category;
    }

    /**
     * 写入留言分类。
     *
     * @param category 1 到 4
     */
    public void setCategory(Integer category) {
        this.category = category;
    }

    /**
     * 留言正文，最长 2000 字。
     *
     * @return 正文
     */
    public String getContent() {
        return content;
    }

    /**
     * 写入留言正文。
     *
     * @param content 正文
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 运行平台。仅 BUG 类填写。
     *
     * @return 平台，未填写时为 null
     */
    public String getOsPlatform() {
        return osPlatform;
    }

    /**
     * 写入运行平台。
     *
     * @param osPlatform 平台，可以为 null
     */
    public void setOsPlatform(String osPlatform) {
        this.osPlatform = osPlatform;
    }

    /**
     * 用户所用工具版本。仅 BUG 类填写。
     *
     * @return 版本号，未填写时为 null
     */
    public String getToolVersion() {
        return toolVersion;
    }

    /**
     * 写入用户所用工具版本。
     *
     * @param toolVersion 版本号，可以为 null
     */
    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }

    /**
     * 复现步骤。仅 BUG 类填写，最长 1000 字。
     *
     * @return 步骤，未填写时为 null
     */
    public String getReproSteps() {
        return reproSteps;
    }

    /**
     * 写入复现步骤。
     *
     * @param reproSteps 步骤，可以为 null
     */
    public void setReproSteps(String reproSteps) {
        this.reproSteps = reproSteps;
    }

    /**
     * 内部处理进度。与审核状态相互独立。
     *
     * @return 1 未处理，2 已处理，3 已跟进，4 无需处理
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 写入内部处理进度。为空时在插入前记为未处理。
     *
     * @param status 1 到 4，可以为 null
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 是否置顶。
     *
     * @return 1 置顶，0 否
     */
    public Integer getPinned() {
        return pinned;
    }

    /**
     * 写入是否置顶。为空时在插入前记为不置顶。
     *
     * @param pinned 1 或 0，可以为 null
     */
    public void setPinned(Integer pinned) {
        this.pinned = pinned;
    }

    /**
     * 审核状态。
     *
     * @return 1 待审核，2 已通过，3 未通过
     */
    public Integer getAuditStatus() {
        return auditStatus;
    }

    /**
     * 写入审核状态。为空时在插入前记为已通过。
     *
     * @param auditStatus 1 到 3，可以为 null
     */
    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    /**
     * 关键词命中标记。
     *
     * @return 1 命中，0 未命中
     */
    public Integer getKeywordFlagged() {
        return keywordFlagged;
    }

    /**
     * 写入关键词命中标记。为空时在插入前记为未命中。
     *
     * @param keywordFlagged 1 或 0，可以为 null
     */
    public void setKeywordFlagged(Integer keywordFlagged) {
        this.keywordFlagged = keywordFlagged;
    }

    /**
     * 审核动作时间。
     *
     * @return 时间，尚未审核动作时为 null
     */
    public LocalDateTime getAuditedAt() {
        return auditedAt;
    }

    /**
     * 写入审核动作时间。
     *
     * @param auditedAt 时间，可以为 null
     */
    public void setAuditedAt(LocalDateTime auditedAt) {
        this.auditedAt = auditedAt;
    }

    /**
     * IP 哈希。不存明文 IP。
     *
     * @return sha256 十六进制
     */
    public String getIpHash() {
        return ipHash;
    }

    /**
     * 写入 IP 哈希。
     *
     * @param ipHash 64 位小写十六进制
     */
    public void setIpHash(String ipHash) {
        this.ipHash = ipHash;
    }

    /**
     * 提交时间。
     *
     * @return 东八区墙钟时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 写入提交时间。为空时在插入前自动补上。
     *
     * @param createdAt 提交时间，可以为 null
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 只输出主键和分类，不输出邮箱、IP 哈希和正文。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "Message{id=" + id + ", category=" + category + '}';
    }
}

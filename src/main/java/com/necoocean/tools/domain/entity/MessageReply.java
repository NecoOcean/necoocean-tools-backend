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
 * 管理员回复。一条留言可以有多条回复。删除留言时由数据库级联删除。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Entity
@Table(name = "message_replies")
public class MessageReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_message_replies_message"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Message message;

    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void fillCreatedAt() {
        if (createdAt == null) {
            createdAt = EntityTimestamps.now();
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
     * 所属留言。
     *
     * @return 留言
     */
    public Message getMessage() {
        return message;
    }

    /**
     * 写入所属留言。
     *
     * @param message 留言
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * 回复正文。
     *
     * @return 正文
     */
    public String getContent() {
        return content;
    }

    /**
     * 写入回复正文。
     *
     * @param content 正文
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 回复时间。
     *
     * @return 东八区墙钟时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 写入回复时间。为空时在插入前自动补上。
     *
     * @param createdAt 回复时间，可以为 null
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 只输出主键。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "MessageReply{id=" + id + '}';
    }
}

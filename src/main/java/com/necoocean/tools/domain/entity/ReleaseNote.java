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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

/**
 * 版本更新日志。同一工具的同一版本只允许一条。删除工具时由数据库级联删除。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Entity
@Table(name = "release_notes", uniqueConstraints = @UniqueConstraint(
        name = "uk_release_notes_tool_version", columnNames = {"tool_id", "version"}))
public class ReleaseNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tool_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_release_notes_tool"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tool tool;

    @Column(nullable = false, length = 32)
    private String version;

    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    @Column(nullable = false)
    private String content;

    @Column(name = "released_at", nullable = false)
    private LocalDateTime releasedAt;

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
     * 所属工具。
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
     * 版本号。
     *
     * @return 版本号
     */
    public String getVersion() {
        return version;
    }

    /**
     * 写入版本号。
     *
     * @param version 版本号
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 更新内容，Markdown。
     *
     * @return 正文
     */
    public String getContent() {
        return content;
    }

    /**
     * 写入更新内容。
     *
     * @param content 正文
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 发布日期。
     *
     * @return 东八区墙钟时间
     */
    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    /**
     * 写入发布日期。
     *
     * @param releasedAt 发布日期
     */
    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    /**
     * 只输出主键、版本号。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "ReleaseNote{id=" + id + ", version=" + version + '}';
    }
}

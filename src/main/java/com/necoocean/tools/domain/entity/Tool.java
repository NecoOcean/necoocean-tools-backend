package com.necoocean.tools.domain.entity;

import java.time.LocalDate;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 工具。不作为接口响应；专题页字段由白名单 DTO 另行装配。slug 创建后不可改。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Entity
@Table(name = "tools")
public class Tool {

    /** 上架。前台可见。 */
    public static final int STATUS_PUBLISHED = 1;

    /** 下架。前台不可访问，数据保留。 */
    public static final int STATUS_UNPUBLISHED = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 120, unique = true)
    private String slug;

    @Column(nullable = false, length = 200)
    private String summary;

    @Column(name = "cover_object_key", length = 255)
    private String coverObjectKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_tools_category"))
    private ToolCategory category;

    @Column(nullable = false, length = 100)
    private String platforms;

    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String tutorial;

    @Column(name = "repo_url", length = 255)
    private String repoUrl;

    @Column(name = "web_url", length = 255)
    private String webUrl;

    @Column(name = "latest_version", length = 32)
    private String latestVersion;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer status;

    @Column(name = "developed_at")
    private LocalDate developedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void fillTimestamps() {
        LocalDateTime now = EntityTimestamps.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void touchUpdatedAt() {
        updatedAt = EntityTimestamps.now();
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
     * 工具名称。
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 写入工具名称。
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 专题页标识。只含英文、数字和连字符，创建后固定。
     *
     * @return 标识
     */
    public String getSlug() {
        return slug;
    }

    /**
     * 写入专题页标识。
     *
     * @param slug 标识
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * 卡片简介。
     *
     * @return 简介
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 写入卡片简介。
     *
     * @param summary 简介
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * 封面对象键。为空时前台使用占位图。
     *
     * @return 对象键，未设置时为 null
     */
    public String getCoverObjectKey() {
        return coverObjectKey;
    }

    /**
     * 写入封面对象键。
     *
     * @param coverObjectKey 对象键，可以为 null
     */
    public void setCoverObjectKey(String coverObjectKey) {
        this.coverObjectKey = coverObjectKey;
    }

    /**
     * 所属分类。
     *
     * @return 分类
     */
    public ToolCategory getCategory() {
        return category;
    }

    /**
     * 写入所属分类。
     *
     * @param category 分类
     */
    public void setCategory(ToolCategory category) {
        this.category = category;
    }

    /**
     * 适用平台。库内以英文逗号分隔，接口层再转成数组。
     *
     * @return 逗号分隔的平台
     */
    public String getPlatforms() {
        return platforms;
    }

    /**
     * 写入适用平台。
     *
     * @param platforms 逗号分隔的平台
     */
    public void setPlatforms(String platforms) {
        this.platforms = platforms;
    }

    /**
     * 使用教程，Markdown。
     *
     * @return 教程，未填写时为 null
     */
    public String getTutorial() {
        return tutorial;
    }

    /**
     * 写入使用教程。
     *
     * @param tutorial 教程，可以为 null
     */
    public void setTutorial(String tutorial) {
        this.tutorial = tutorial;
    }

    /**
     * 站外代码仓库地址。不代理、不嵌入。
     *
     * @return 地址，未填写时为 null
     */
    public String getRepoUrl() {
        return repoUrl;
    }

    /**
     * 写入代码仓库地址。
     *
     * @param repoUrl 地址，可以为 null
     */
    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    /**
     * 站外 Web 工具地址。
     *
     * @return 地址，未填写时为 null
     */
    public String getWebUrl() {
        return webUrl;
    }

    /**
     * 写入 Web 工具地址。
     *
     * @param webUrl 地址，可以为 null
     */
    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    /**
     * 当前推荐版本。与资源文件的最新标记在同一事务里更新。
     *
     * @return 版本号，尚无文件时为 null
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * 写入当前推荐版本。
     *
     * @param latestVersion 版本号，可以为 null
     */
    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

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
     * @param status 1 上架，0 下架
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 作者手填的开发时间。不参与排序。
     *
     * @return 日期，未填写时为 null
     */
    public LocalDate getDevelopedAt() {
        return developedAt;
    }

    /**
     * 写入开发时间。
     *
     * @param developedAt 日期，可以为 null
     */
    public void setDevelopedAt(LocalDate developedAt) {
        this.developedAt = developedAt;
    }

    /**
     * 创建时间。
     *
     * @return 东八区墙钟时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 写入创建时间。为空时在插入前自动补上。
     *
     * @param createdAt 创建时间，可以为 null
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 内容最后修改时间。首页卡片用它，不接受客户端手填。
     *
     * @return 东八区墙钟时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 写入最后修改时间。为空时在插入前自动补上，更新时自动刷新。
     *
     * @param updatedAt 修改时间，可以为 null
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 只输出主键、标识和状态。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "Tool{id=" + id + ", slug=" + slug + ", status=" + status + '}';
    }
}

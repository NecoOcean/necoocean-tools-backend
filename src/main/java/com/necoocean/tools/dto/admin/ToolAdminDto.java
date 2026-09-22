package com.necoocean.tools.dto.admin;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.necoocean.tools.dto.publicapi.CategoryDto;

/**
 * 后台工具详情。含下架状态与全部可编辑字段。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class ToolAdminDto {

    private final Integer id;
    private final String name;
    private final String slug;
    private final String summary;
    private final String coverUrl;
    private final CategoryDto category;
    private final List<String> platforms;
    private final String tutorial;
    private final String latestVersion;
    private final String repoUrl;
    private final String webUrl;
    private final Integer status;
    private final LocalDate developedAt;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    /**
     * @param id            主键
     * @param name          名称
     * @param slug          标识
     * @param summary       简介
     * @param coverUrl      封面，对象存储未接入时为 null
     * @param category      分类
     * @param platforms     平台
     * @param tutorial      教程
     * @param latestVersion 推荐版本
     * @param repoUrl       仓库
     * @param webUrl        网页
     * @param status        上下架
     * @param developedAt   开发日期
     * @param createdAt     创建时间
     * @param updatedAt     更新时间
     */
    public ToolAdminDto(Integer id, String name, String slug, String summary, String coverUrl, CategoryDto category,
            List<String> platforms, String tutorial, String latestVersion, String repoUrl, String webUrl,
            Integer status, LocalDate developedAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.summary = summary;
        this.coverUrl = coverUrl;
        this.category = category;
        this.platforms = platforms;
        this.tutorial = tutorial;
        this.latestVersion = latestVersion;
        this.repoUrl = repoUrl;
        this.webUrl = webUrl;
        this.status = status;
        this.developedAt = developedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 读取字段。
     *
     * @return 值
     */
    public Integer getId() {
        return id;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getName() {
        return name;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getSlug() {
        return slug;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getSummary() {
        return summary;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getCoverUrl() {
        return coverUrl;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public CategoryDto getCategory() {
        return category;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public List<String> getPlatforms() {
        return platforms;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getTutorial() {
        return tutorial;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getLatestVersion() {
        return latestVersion;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getRepoUrl() {
        return repoUrl;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getWebUrl() {
        return webUrl;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public Integer getStatus() {
        return status;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public LocalDate getDevelopedAt() {
        return developedAt;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "ToolAdminDto{id=" + id + ", slug=" + slug + '}';
    }
}
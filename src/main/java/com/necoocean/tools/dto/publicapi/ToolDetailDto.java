package com.necoocean.tools.dto.publicapi;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 专题页头部。已下架的工具不会装配成这个对象。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class ToolDetailDto {

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

    private final LocalDate developedAt;

    private final OffsetDateTime createdAt;

    private final OffsetDateTime updatedAt;

    /**
     * @param id            工具主键
     * @param name          名称
     * @param slug          专题页标识
     * @param summary       简介
     * @param coverUrl      封面地址，对象存储未接入时为 null
     * @param category      分类
     * @param platforms     平台列表
     * @param tutorial      教程，可以为 null
     * @param latestVersion 推荐版本，可以为 null
     * @param repoUrl       仓库外链，可以为 null
     * @param webUrl        网页外链，可以为 null
     * @param developedAt   开发日期，可以为 null
     * @param createdAt     创建时间
     * @param updatedAt     更新时间
     */
    public ToolDetailDto(Integer id, String name, String slug, String summary, String coverUrl, CategoryDto category,
            List<String> platforms, String tutorial, String latestVersion, String repoUrl, String webUrl,
            LocalDate developedAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
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
        this.developedAt = developedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 工具主键。
     *
     * @return 主键
     */
    public Integer getId() {
        return id;
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
     * 专题页标识。
     *
     * @return 标识
     */
    public String getSlug() {
        return slug;
    }

    /**
     * 简介。
     *
     * @return 简介
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 封面地址。没有公开域名时为 null。
     *
     * @return 地址或 null
     */
    public String getCoverUrl() {
        return coverUrl;
    }

    /**
     * 所属分类。
     *
     * @return 分类
     */
    public CategoryDto getCategory() {
        return category;
    }

    /**
     * 适用平台。
     *
     * @return 平台列表
     */
    public List<String> getPlatforms() {
        return platforms;
    }

    /**
     * 教程正文。
     *
     * @return Markdown，可以为 null
     */
    public String getTutorial() {
        return tutorial;
    }

    /**
     * 推荐版本。
     *
     * @return 版本，可以为 null
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * 代码仓库外链。
     *
     * @return 地址，可以为 null
     */
    public String getRepoUrl() {
        return repoUrl;
    }

    /**
     * 在线页面外链。
     *
     * @return 地址，可以为 null
     */
    public String getWebUrl() {
        return webUrl;
    }

    /**
     * 作者填写的开发日期。不参与排序。
     *
     * @return 日期，可以为 null
     */
    public LocalDate getDevelopedAt() {
        return developedAt;
    }

    /**
     * 创建时间。
     *
     * @return 带 +08:00 的时间
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 更新时间。
     *
     * @return 带 +08:00 的时间
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 只输出主键和标识。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "ToolDetailDto{id=" + id + ", slug=" + slug + '}';
    }
}

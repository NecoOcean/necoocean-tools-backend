package com.necoocean.tools.dto.publicapi;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 首页工具卡片。不含教程和文件列表。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class ToolCardDto {

    private final Integer id;

    private final String name;

    private final String slug;

    private final String summary;

    private final String coverUrl;

    private final CategoryDto category;

    private final List<String> platforms;

    private final String latestVersion;

    private final OffsetDateTime updatedAt;

    /**
     * @param id            工具主键
     * @param name          名称
     * @param slug          专题页标识
     * @param summary       简介
     * @param coverUrl      封面地址，对象存储未接入时为 null
     * @param category      分类
     * @param platforms     平台列表
     * @param latestVersion 推荐版本，可以为 null
     * @param updatedAt     更新时间
     */
    public ToolCardDto(Integer id, String name, String slug, String summary, String coverUrl, CategoryDto category,
            List<String> platforms, String latestVersion, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.summary = summary;
        this.coverUrl = coverUrl;
        this.category = category;
        this.platforms = platforms;
        this.latestVersion = latestVersion;
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
     * 卡片简介。
     *
     * @return 简介
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 封面地址。没有公开域名时为 null，不返回对象键。
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
     * 推荐版本。
     *
     * @return 版本，可以为 null
     */
    public String getLatestVersion() {
        return latestVersion;
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
        return "ToolCardDto{id=" + id + ", slug=" + slug + '}';
    }
}

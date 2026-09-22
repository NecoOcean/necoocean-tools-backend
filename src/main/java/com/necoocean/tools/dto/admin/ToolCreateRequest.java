package com.necoocean.tools.dto.admin;

import java.time.LocalDate;
import java.util.List;

/**
 * 新建工具请求。slug 创建后不可改。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class ToolCreateRequest {

    private String name;

    private String slug;

    private String summary;

    private Integer categoryId;

    private List<String> platforms;

    private String tutorial;

    private String repoUrl;

    private String webUrl;

    private LocalDate developedAt;

    private Integer status;

    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getName() {
        return name;
    }
    /**
     * 写入字段。
     *
     * @param name 值
     */
    public void setName(String name) {
        this.name = name;
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
     * 写入字段。
     *
     * @param slug 值
     */
    public void setSlug(String slug) {
        this.slug = slug;
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
     * 写入字段。
     *
     * @param summary 值
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * 读取字段。
     *
     * @return 值
     */
    public Integer getCategoryId() {
        return categoryId;
    }
    /**
     * 写入字段。
     *
     * @param categoryId 值
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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
     * 写入字段。
     *
     * @param platforms 值
     */
    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
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
     * 写入字段。
     *
     * @param tutorial 值
     */
    public void setTutorial(String tutorial) {
        this.tutorial = tutorial;
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
     * 写入字段。
     *
     * @param repoUrl 值
     */
    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
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
     * 写入字段。
     *
     * @param webUrl 值
     */
    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
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
     * 写入字段。
     *
     * @param developedAt 值
     */
    public void setDevelopedAt(LocalDate developedAt) {
        this.developedAt = developedAt;
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
     * 写入字段。
     *
     * @param status 值
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "ToolCreateRequest{slug=" + slug + '}';
    }
}
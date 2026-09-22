package com.necoocean.tools.dto.admin;

/**
 * 后台分类。含工具数。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class CategoryAdminDto {

    private final Integer id;
    private final String name;
    private final Integer sortOrder;
    private final Long toolCount;

    /**
     * @param id        主键
     * @param name      名称
     * @param sortOrder 排序
     * @param toolCount 工具数
     */
    public CategoryAdminDto(Integer id, String name, Integer sortOrder, Long toolCount) {
        this.id = id;
        this.name = name;
        this.sortOrder = sortOrder;
        this.toolCount = toolCount;
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
    public Integer getSortOrder() {
        return sortOrder;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public Long getToolCount() {
        return toolCount;
    }
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "CategoryAdminDto{id=" + id + '}';
    }
}
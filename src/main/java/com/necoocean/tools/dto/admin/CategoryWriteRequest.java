package com.necoocean.tools.dto.admin;

/**
 * 分类新增与编辑请求。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class CategoryWriteRequest {

    private String name;

    private Integer sortOrder;

    /**
     * 分类名称。
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }
    /**
     * 写入分类名称。
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 展示顺序。新增时可空，默认最大值加一。
     *
     * @return 排序值
     */
    public Integer getSortOrder() {
        return sortOrder;
    }
    /**
     * 写入展示顺序。
     *
     * @param sortOrder 排序值
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "CategoryWriteRequest{name=" + name + '}';
    }
}
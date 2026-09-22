package com.necoocean.tools.dto.publicapi;

/**
 * 工具所属分类。只含前台展示需要的字段。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class CategoryDto {

    private final Integer id;

    private final String name;

    /**
     * @param id   分类主键
     * @param name 分类名称
     */
    public CategoryDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 分类主键。
     *
     * @return 主键
     */
    public Integer getId() {
        return id;
    }

    /**
     * 分类名称。
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 只输出主键。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "CategoryDto{id=" + id + '}';
    }
}

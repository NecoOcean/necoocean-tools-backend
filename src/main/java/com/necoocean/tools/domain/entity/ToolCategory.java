package com.necoocean.tools.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 工具分类。预置「其他工具」是兜底分类，删除保护由业务层负责。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Entity
@Table(name = "tool_categories")
public class ToolCategory {

    /** 兜底分类名称。任何情况下不可删除。 */
    public static final String FALLBACK_NAME = "其他工具";

    /** 初始化脚本写入的兜底分类主键。 */
    public static final int FALLBACK_ID = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

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
     * 分类名称。同名即冲突。
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
     * 首页筛选的展示顺序。
     *
     * @return 排序值，小的在前
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
     * 只输出主键和名称。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "ToolCategory{id=" + id + ", name=" + name + '}';
    }
}

package com.necoocean.tools.domain.repository;

import java.util.List;
import java.util.Optional;

import com.necoocean.tools.domain.entity.ToolCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 分类存储。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public interface ToolCategoryRepository extends JpaRepository<ToolCategory, Integer> {

    /**
     * 按名称查找分类。名称唯一。
     *
     * @param name 分类名称
     * @return 分类；不存在时为空
     */
    Optional<ToolCategory> findByName(String name);

    /**
     * 名称是否已被占用。
     *
     * @param name 分类名称
     * @return 已存在时为 true
     */
    boolean existsByName(String name);

    /**
     * 全部类别，按展示顺序升序。
     *
     * @return 分类列表
     */
    @Query("select category from ToolCategory category order by category.sortOrder asc, category.id asc")
    List<ToolCategory> findAllOrdered();

    /**
     * 当前最大 sort_order。无记录时为空。
     *
     * @return 最大值
     */
    @Query("select max(category.sortOrder) from ToolCategory category")
    Optional<Integer> findMaxSortOrder();
}

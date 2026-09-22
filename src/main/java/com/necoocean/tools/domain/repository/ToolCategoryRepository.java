package com.necoocean.tools.domain.repository;

import java.util.Optional;

import com.necoocean.tools.domain.entity.ToolCategory;

import org.springframework.data.jpa.repository.JpaRepository;

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
}

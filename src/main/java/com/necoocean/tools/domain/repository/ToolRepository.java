package com.necoocean.tools.domain.repository;

import java.util.Optional;

import com.necoocean.tools.domain.entity.Tool;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 工具存储。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public interface ToolRepository extends JpaRepository<Tool, Integer> {

    /**
     * 按专题页标识查找工具。
     *
     * @param slug 创建后不变的标识
     * @return 工具；不存在时为空
     */
    Optional<Tool> findBySlug(String slug);
}

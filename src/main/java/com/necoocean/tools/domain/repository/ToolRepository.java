package com.necoocean.tools.domain.repository;

import java.util.Optional;

import com.necoocean.tools.domain.entity.Tool;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * 已上架工具。分类和关键字同时生效，按更新时间倒序。
     *
     * @param status     上架状态
     * @param categoryId 分类，空表示不限
     * @param keyword    已转义的包含匹配关键字，空表示不限
     * @param pageable   分页，排序以查询语句为准
     * @return 当前页
     */
    @Query("""
            select tool from Tool tool
            where tool.status = :status
              and (:categoryId is null or tool.category.id = :categoryId)
              and (:keyword is null
                   or lower(tool.name) like lower(concat('%', :keyword, '%')) escape '\\'
                   or lower(tool.summary) like lower(concat('%', :keyword, '%')) escape '\\')
            order by tool.updatedAt desc
            """)
    Page<Tool> findPublished(@Param("status") Integer status, @Param("categoryId") Integer categoryId,
            @Param("keyword") String keyword, Pageable pageable);

    /**
     * 后台工具列表。含下架，筛选条件同时生效，按更新时间倒序。
     *
     * @param status     上下架，空表示不限
     * @param categoryId 分类，空表示不限
     * @param keyword    已转义的包含匹配关键字，空表示不限
     * @param pageable   分页，排序以查询语句为准
     * @return 当前页
     */
    @Query("""
            select tool from Tool tool
            where (:status is null or tool.status = :status)
              and (:categoryId is null or tool.category.id = :categoryId)
              and (:keyword is null
                   or lower(tool.name) like lower(concat('%', :keyword, '%')) escape '\\'
                   or lower(tool.summary) like lower(concat('%', :keyword, '%')) escape '\\')
            order by tool.updatedAt desc
            """)
    Page<Tool> findAdmin(@Param("status") Integer status, @Param("categoryId") Integer categoryId,
            @Param("keyword") String keyword, Pageable pageable);

    /**
     * 某分类下的工具数。
     *
     * @param categoryId 分类主键
     * @return 条数
     */
    @Query("select count(tool) from Tool tool where tool.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Integer categoryId);

    /**
     * 专题页标识是否已被占用。
     *
     * @param slug 标识
     * @return 已存在时为 true
     */
    boolean existsBySlug(String slug);
}

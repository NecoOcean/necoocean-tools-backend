package com.necoocean.tools.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.necoocean.tools.domain.entity.Message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 留言存储。公开列表只查已通过审核的记录。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public interface MessageRepository extends JpaRepository<Message, Integer> {

    /**
     * 某工具已通过审核的留言。置顶在前，其余按提交时间倒序。
     *
     * @param toolId      工具主键
     * @param auditStatus 已通过
     * @param pageable    分页，排序以查询语句为准
     * @return 当前页
     */
    @Query("""
            select message from Message message
            where message.tool.id = :toolId and message.auditStatus = :auditStatus
            order by message.pinned desc, message.createdAt desc
            """)
    Page<Message> findApprovedByToolId(@Param("toolId") Integer toolId, @Param("auditStatus") Integer auditStatus,
            Pageable pageable);

    /**
     * 后台留言列表。命中关键词优先，其余按提交时间正序。
     *
     * @param toolId         工具，空表示不限
     * @param category       留言分类，空表示不限
     * @param status         处理状态，空表示不限
     * @param auditStatus    审核状态，空表示不限
     * @param keywordFlagged 关键词命中，空表示不限
     * @param hasEmail       true 仅有邮箱，false 仅无邮箱，空表示不限
     * @param dateFrom       起始时间，含，空表示不限
     * @param dateTo         截止时间，含，空表示不限
     * @param pageable       分页，排序以查询语句为准
     * @return 当前页
     */
    @Query("""
            select message from Message message
            where (:toolId is null or message.tool.id = :toolId)
              and (:category is null or message.category = :category)
              and (:status is null or message.status = :status)
              and (:auditStatus is null or message.auditStatus = :auditStatus)
              and (:keywordFlagged is null or message.keywordFlagged = :keywordFlagged)
              and (:hasEmail is null
                   or (:hasEmail = true and message.contactEmail is not null)
                   or (:hasEmail = false and message.contactEmail is null))
              and (:dateFrom is null or message.createdAt >= :dateFrom)
              and (:dateTo is null or message.createdAt <= :dateTo)
            order by message.keywordFlagged desc, message.createdAt asc
            """)
    Page<Message> findAdmin(@Param("toolId") Integer toolId, @Param("category") Integer category,
            @Param("status") Integer status, @Param("auditStatus") Integer auditStatus,
            @Param("keywordFlagged") Integer keywordFlagged, @Param("hasEmail") Boolean hasEmail,
            @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    /**
     * 各工具留言统计。按工具主键升序。
     *
     * @param bugCategory        BUG 分类枚举
     * @param suggestionCategory 功能建议枚举
     * @return 每行：toolId, toolName, total, bugCount, suggestionCount
     */
    @Query("""
            select message.tool.id, message.tool.name, count(message),
                   sum(case when message.category = :bugCategory then 1 else 0 end),
                   sum(case when message.category = :suggestionCategory then 1 else 0 end)
            from Message message
            group by message.tool.id, message.tool.name
            order by message.tool.id asc
            """)
    List<Object[]> countStatsByTool(@Param("bugCategory") Integer bugCategory,
            @Param("suggestionCategory") Integer suggestionCategory);

    /**
     * 待审核留言。
     *
     * @param auditStatus 待审核
     * @return 待审列表
     */
    @Query("select message from Message message where message.auditStatus = :auditStatus")
    List<Message> findByAuditStatus(@Param("auditStatus") Integer auditStatus);

    /**
     * 一键通过全部待审。
     *
     * @param pending  待审核
     * @param approved 已通过
     * @param auditedAt 审核时间
     * @return 受影响行数
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Message message
            set message.auditStatus = :approved, message.auditedAt = :auditedAt
            where message.auditStatus = :pending
            """)
    int approveAllPending(@Param("pending") Integer pending, @Param("approved") Integer approved,
            @Param("auditedAt") LocalDateTime auditedAt);

    /**
     * 同一 IP 哈希在时间窗内的留言条数，含尚未公开的记录。
     *
     * @param ipHash 哈希，不能为空
     * @param since  窗口起点，含该时刻
     * @return 条数
     */
    @Query("""
            select count(message) from Message message
            where message.ipHash = :ipHash and message.createdAt >= :since
            """)
    long countRecentByIpHash(@Param("ipHash") String ipHash, @Param("since") LocalDateTime since);

    /**
     * 同一 IP 哈希对同一工具在时间窗内的留言条数。
     *
     * @param ipHash 哈希，不能为空
     * @param toolId 工具主键
     * @param since  窗口起点，含该时刻
     * @return 条数
     */
    @Query("""
            select count(message) from Message message
            where message.ipHash = :ipHash and message.tool.id = :toolId and message.createdAt >= :since
            """)
    long countRecentByIpHashAndTool(@Param("ipHash") String ipHash, @Param("toolId") Integer toolId,
            @Param("since") LocalDateTime since);
}

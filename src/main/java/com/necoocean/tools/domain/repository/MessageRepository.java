package com.necoocean.tools.domain.repository;

import com.necoocean.tools.domain.entity.Message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}

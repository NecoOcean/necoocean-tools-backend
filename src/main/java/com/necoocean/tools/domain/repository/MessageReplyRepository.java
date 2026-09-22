package com.necoocean.tools.domain.repository;

import java.util.List;

import com.necoocean.tools.domain.entity.MessageReply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 管理员回复存储。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public interface MessageReplyRepository extends JpaRepository<MessageReply, Integer> {

    /**
     * 按留言列出回复，早提交的在前。
     *
     * @param messageId 留言主键
     * @return 回复列表，没有时为空列表
     */
    @Query("select reply from MessageReply reply where reply.message.id = :messageId order by reply.createdAt asc")
    List<MessageReply> findByMessageIdOrderByCreatedAtAsc(@Param("messageId") Integer messageId);
}

package com.necoocean.tools.domain.repository;

import com.necoocean.tools.domain.entity.Message;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 留言存储。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public interface MessageRepository extends JpaRepository<Message, Integer> {
}

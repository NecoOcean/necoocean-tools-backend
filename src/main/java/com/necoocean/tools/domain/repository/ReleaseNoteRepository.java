package com.necoocean.tools.domain.repository;

import java.util.List;

import com.necoocean.tools.domain.entity.ReleaseNote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 更新日志存储。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public interface ReleaseNoteRepository extends JpaRepository<ReleaseNote, Integer> {

    /**
     * 按工具列出更新日志，发布时间新的在前。
     *
     * @param toolId 工具主键
     * @return 日志列表，没有时为空列表
     */
    @Query("select note from ReleaseNote note where note.tool.id = :toolId order by note.releasedAt desc")
    List<ReleaseNote> findByToolIdOrderByReleasedAtDesc(@Param("toolId") Integer toolId);
}

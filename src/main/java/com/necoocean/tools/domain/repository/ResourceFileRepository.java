package com.necoocean.tools.domain.repository;

import java.util.Optional;

import com.necoocean.tools.domain.entity.ResourceFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 资源文件元数据存储。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public interface ResourceFileRepository extends JpaRepository<ResourceFile, Integer> {

    /**
     * 查找某工具标记为最新的文件。同一工具至多一条。
     *
     * @param toolId 工具主键
     * @param latest 1 表示最新
     * @return 文件；没有时为空
     */
    @Query("select file from ResourceFile file where file.tool.id = :toolId and file.latest = :latest")
    Optional<ResourceFile> findByToolIdAndLatest(@Param("toolId") Integer toolId, @Param("latest") Integer latest);
}

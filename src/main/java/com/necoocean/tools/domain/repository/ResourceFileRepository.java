package com.necoocean.tools.domain.repository;

import java.util.List;
import java.util.Optional;

import com.necoocean.tools.domain.entity.ResourceFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * 某工具的全部文件。推荐版本在前，其余按上传时间倒序。
     *
     * @param toolId 工具主键
     * @return 文件列表，没有时为空列表
     */
    @Query("""
            select file from ResourceFile file
            where file.tool.id = :toolId
            order by file.latest desc, file.createdAt desc
            """)
    List<ResourceFile> findByToolIdForPublic(@Param("toolId") Integer toolId);

    /**
     * 某工具全部文件，按上传时间倒序。删除推荐版本后用于自动指定新推荐。
     *
     * @param toolId 工具主键
     * @return 文件列表
     */
    @Query("select file from ResourceFile file where file.tool.id = :toolId order by file.createdAt desc")
    List<ResourceFile> findByToolIdOrderByCreatedAtDesc(@Param("toolId") Integer toolId);

    /**
     * 汇总某工具文件总字节数。无文件时返回 null。
     *
     * @param toolId 工具主键
     * @return 总字节数
     */
    @Query("select coalesce(sum(file.fileSize), 0) from ResourceFile file where file.tool.id = :toolId")
    Long sumFileSizeByToolId(@Param("toolId") Integer toolId);

    /**
     * 某工具文件条数。
     *
     * @param toolId 工具主键
     * @return 条数
     */
    @Query("select count(file) from ResourceFile file where file.tool.id = :toolId")
    long countByToolId(@Param("toolId") Integer toolId);

    /**
     * 清掉某工具全部推荐标记。须在同一事务内再置新推荐。
     *
     * @param toolId 工具主键
     * @param latest 非推荐取值，一般为 0
     * @return 受影响行数
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ResourceFile file set file.latest = :latest where file.tool.id = :toolId")
    int clearLatestByToolId(@Param("toolId") Integer toolId, @Param("latest") Integer latest);
}

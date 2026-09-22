package com.necoocean.tools.domain.repository;

import com.necoocean.tools.domain.entity.ExportAuditLog;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 导出审计存储。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public interface ExportAuditLogRepository extends JpaRepository<ExportAuditLog, Long> {
}

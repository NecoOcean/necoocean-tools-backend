-- 导出审计。记录谁在何时以何种参数导出，便于追溯是否含邮箱。
CREATE TABLE export_audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    operator VARCHAR(64) NOT NULL COMMENT '管理员用户名',
    export_type VARCHAR(32) NOT NULL COMMENT 'tools / messages / replies / all',
    export_format VARCHAR(16) NOT NULL COMMENT 'csv / json',
    include_email TINYINT NOT NULL COMMENT '0 不含 1 含',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_export_audit_created (created_at),
    CONSTRAINT chk_export_audit_include_email
        CHECK (include_email IN (0, 1)),
    CONSTRAINT chk_export_audit_type
        CHECK (export_type IN ('tools', 'messages', 'replies', 'all')),
    CONSTRAINT chk_export_audit_format
        CHECK (export_format IN ('csv', 'json'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '数据导出审计';

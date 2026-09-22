-- 个人工具展示站 · 表结构
-- 事实源：PRD §8（8 实体 / 5 条外键）与 §8.9 索引。
--
-- 用法：由 Flyway 在已经建好的空库中执行。建库脚本是 db/create_database.sql。
-- 本文件不包含 CREATE DATABASE、CREATE USER。
-- 要求：MySQL 8.4，库字符集 utf8mb4，排序规则 utf8mb4_0900_ai_ci。
-- 时间按东八区墙钟写入 DATETIME，不带时区列。

SET NAMES utf8mb4;
SET time_zone = '+08:00';

-- ---------------------------------------------------------------------------
-- 分类。无外键，必须先于 tools。
-- ---------------------------------------------------------------------------
CREATE TABLE tool_categories (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名，同名即冲突',
    sort_order INT NOT NULL COMMENT '首页筛选展示顺序',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tool_categories_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '工具分类';

-- ---------------------------------------------------------------------------
-- 工具。外键 1/5：category_id → tool_categories。
-- 分类下仍有工具时拒绝删除，因此不用 ON DELETE CASCADE。
-- ---------------------------------------------------------------------------
CREATE TABLE tools (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '工具名称',
    slug VARCHAR(120) NOT NULL COMMENT '专题页 URL，创建后不可改',
    summary VARCHAR(200) NOT NULL COMMENT '卡片简介',
    cover_object_key VARCHAR(255) NULL COMMENT '封面对象键 covers/{tool_id}/{uuid}.{ext}',
    category_id INT NOT NULL,
    platforms VARCHAR(100) NOT NULL COMMENT '库内逗号分隔：Windows,Android,Mac,通用',
    tutorial TEXT NULL COMMENT '使用教程 Markdown',
    repo_url VARCHAR(255) NULL COMMENT '站外代码仓库，不代理',
    web_url VARCHAR(255) NULL COMMENT '站外 Web 工具，不嵌入',
    latest_version VARCHAR(32) NULL COMMENT '冗余推荐版本，与 is_latest 同事务更新',
    status TINYINT NOT NULL COMMENT '1 上架，0 下架',
    developed_at DATE NULL COMMENT '作者手填的开发时间，不参与排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '系统维护，首页卡片更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tools_slug (slug),
    KEY idx_tools_category_id (category_id),
    CONSTRAINT fk_tools_category
        FOREIGN KEY (category_id) REFERENCES tool_categories (id),
    CONSTRAINT chk_tools_slug
        CHECK (slug REGEXP '^[A-Za-z0-9]+(-[A-Za-z0-9]+)*$'),
    CONSTRAINT chk_tools_status
        CHECK (status IN (0, 1))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '工具';

-- ---------------------------------------------------------------------------
-- 更新日志。外键 2/5。删除工具时级联删除。
-- ---------------------------------------------------------------------------
CREATE TABLE release_notes (
    id INT NOT NULL AUTO_INCREMENT,
    tool_id INT NOT NULL,
    version VARCHAR(32) NOT NULL,
    content TEXT NOT NULL COMMENT '更新内容 Markdown',
    released_at DATETIME NOT NULL COMMENT '发布日期',
    PRIMARY KEY (id),
    UNIQUE KEY uk_release_notes_tool_version (tool_id, version),
    CONSTRAINT fk_release_notes_tool
        FOREIGN KEY (tool_id) REFERENCES tools (id)
        ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '版本更新日志';

-- ---------------------------------------------------------------------------
-- 资源文件元数据。二进制在 COS，不入库。外键 3/5。
-- ---------------------------------------------------------------------------
CREATE TABLE resource_files (
    id INT NOT NULL AUTO_INCREMENT,
    tool_id INT NOT NULL,
    version VARCHAR(32) NOT NULL,
    display_name VARCHAR(255) NOT NULL COMMENT '原始文件名，只用于下载响应头',
    object_key VARCHAR(255) NOT NULL COMMENT '对象键，不含桶名与域名',
    ext VARCHAR(16) NOT NULL COMMENT '扩展名，不含点',
    file_size BIGINT NOT NULL COMMENT '字节',
    sha256 CHAR(64) NOT NULL COMMENT '小写十六进制',
    platform VARCHAR(20) NULL COMMENT 'Windows / Android / Mac / 通用',
    is_latest TINYINT NOT NULL COMMENT '同工具至多一条为 1',
    download_count INT NOT NULL DEFAULT 0 COMMENT '一期只累加，不展示',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (id),
    KEY idx_resource_files_tool_version (tool_id, version),
    CONSTRAINT fk_resource_files_tool
        FOREIGN KEY (tool_id) REFERENCES tools (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_resource_files_sha256
        CHECK (sha256 REGEXP '^[0-9a-f]{64}$'),
    CONSTRAINT chk_resource_files_is_latest
        CHECK (is_latest IN (0, 1)),
    CONSTRAINT chk_resource_files_platform
        CHECK (platform IS NULL OR platform IN ('Windows', 'Android', 'Mac', '通用'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '资源文件元数据';

-- 非最新行在索引表达式中为 NULL，唯一索引允许多个 NULL，从而只限制「每个工具一条 is_latest = 1」。
CREATE UNIQUE INDEX uk_resource_files_one_latest
    ON resource_files ((CASE WHEN is_latest = 1 THEN tool_id ELSE NULL END));

-- ---------------------------------------------------------------------------
-- 留言。外键 4/5。audit_status 默认 2（先发后审），索引一期即建。
-- ---------------------------------------------------------------------------
CREATE TABLE messages (
    id INT NOT NULL AUTO_INCREMENT,
    tool_id INT NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    contact_email VARCHAR(254) NULL COMMENT '选填，仅管理员可见，不建唯一索引',
    category TINYINT NOT NULL COMMENT '1 使用体验 2 功能建议 3 BUG报错 4 问题咨询',
    content VARCHAR(2000) NOT NULL,
    os_platform VARCHAR(50) NULL COMMENT '仅 BUG 类',
    tool_version VARCHAR(32) NULL COMMENT '仅 BUG 类',
    repro_steps VARCHAR(1000) NULL COMMENT '仅 BUG 类',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '内部进度：1 未处理 2 已处理 3 已跟进 4 无需处理',
    is_pinned TINYINT NOT NULL DEFAULT 0 COMMENT '1 置顶',
    audit_status TINYINT NOT NULL DEFAULT 2 COMMENT '1 待审核 2 已通过 3 未通过；一期默认 2',
    keyword_flagged TINYINT NOT NULL DEFAULT 0 COMMENT '1 命中关键词，不拦截',
    audited_at DATETIME NULL,
    ip_hash CHAR(64) NOT NULL COMMENT 'sha256(ip+盐)，不存明文 IP',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_messages_tool_created (tool_id, created_at),
    KEY idx_messages_audit_created (audit_status, created_at),
    KEY idx_messages_pinned_created (is_pinned, created_at),
    KEY idx_messages_status (status),
    KEY idx_messages_category (category),
    CONSTRAINT fk_messages_tool
        FOREIGN KEY (tool_id) REFERENCES tools (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_messages_category
        CHECK (category IN (1, 2, 3, 4)),
    CONSTRAINT chk_messages_status
        CHECK (status IN (1, 2, 3, 4)),
    CONSTRAINT chk_messages_is_pinned
        CHECK (is_pinned IN (0, 1)),
    CONSTRAINT chk_messages_audit_status
        CHECK (audit_status IN (1, 2, 3)),
    CONSTRAINT chk_messages_keyword_flagged
        CHECK (keyword_flagged IN (0, 1)),
    CONSTRAINT chk_messages_ip_hash
        CHECK (ip_hash REGEXP '^[0-9a-f]{64}$')
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '访客留言';

-- ---------------------------------------------------------------------------
-- 管理员回复。外键 5/5。一条留言可有多条回复。
-- ---------------------------------------------------------------------------
CREATE TABLE message_replies (
    id INT NOT NULL AUTO_INCREMENT,
    message_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_message_replies_message_id (message_id),
    CONSTRAINT fk_message_replies_message
        FOREIGN KEY (message_id) REFERENCES messages (id)
        ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '管理员回复';

-- ---------------------------------------------------------------------------
-- 管理员。与业务表无外键。不预置账号，避免把口令写进脚本。
-- ---------------------------------------------------------------------------
CREATE TABLE admin_users (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL COMMENT '加盐哈希，不存明文',
    last_login_at DATETIME NULL,
    failed_attempts INT NOT NULL DEFAULT 0 COMMENT '连续失败次数，成功登录后清零',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_users_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '管理员账号';

-- ---------------------------------------------------------------------------
-- 站点配置。KV，主键是 key。未在白名单内的键由应用拒绝写入。
-- ---------------------------------------------------------------------------
CREATE TABLE site_settings (
    `key` VARCHAR(100) NOT NULL,
    `value` TEXT NULL COMMENT '统一存字符串，由应用按键解析',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '站点配置';

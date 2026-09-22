-- 预置数据。在 V1__init_schema.sql 之后、空库上执行一次。
-- 不插入管理员。账号和口令由建库之后在本地写入，不放进版本库。

SET NAMES utf8mb4;
SET time_zone = '+08:00';

INSERT INTO tool_categories (id, name, sort_order) VALUES
    (1, 'APP应用', 1),
    (2, 'Python脚本', 2),
    (3, '桌面工具', 3),
    (4, '其他工具', 4);

ALTER TABLE tool_categories AUTO_INCREMENT = 5;

-- 「其他工具」(id = 4) 是兜底分类，应用层必须拒绝删除。
-- 文案类配置留空，用 NULL 表示未填写，不用空字符串。
INSERT INTO site_settings (`key`, `value`, updated_at) VALUES
    ('site_name', NULL, CURRENT_TIMESTAMP),
    ('home_intro', NULL, CURRENT_TIMESTAMP),
    ('copyright', NULL, CURRENT_TIMESTAMP),
    ('icp_number', NULL, CURRENT_TIMESTAMP),
    ('announcement', NULL, CURRENT_TIMESTAMP),
    ('about_content', NULL, CURRENT_TIMESTAMP),
    ('privacy_content', NULL, CURRENT_TIMESTAMP),
    ('message_board_enabled', 'true', CURRENT_TIMESTAMP),
    ('message_audit_mode', 'post', CURRENT_TIMESTAMP);

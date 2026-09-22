-- 登录锁定窗口。连续失败 5 次后，从最近一次失败时间起锁定 15 分钟。
-- failed_attempts 只记次数，没有这个时间列就无法判断锁定期是否已满。

SET NAMES utf8mb4;
SET time_zone = '+08:00';

ALTER TABLE admin_users
    ADD COLUMN last_failed_at DATETIME NULL COMMENT '最近一次登录失败时间，用于 15 分钟锁定窗口';

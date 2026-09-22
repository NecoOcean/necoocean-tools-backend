-- 个人工具展示站 · 建库
-- 在已经连上 MySQL、尚未选中业务库时执行。不要放进 Flyway。
-- 表和预置数据由应用启动时执行 db/migration/V1、V2。
-- 不创建账号，不写口令。

CREATE DATABASE IF NOT EXISTS necoocean_tools
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

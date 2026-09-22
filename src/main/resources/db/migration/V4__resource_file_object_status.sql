-- 资源文件对象状态：上传中 / 可用 / 异常（库有记录但对象缺失）。
ALTER TABLE resource_files
    ADD COLUMN object_status TINYINT NOT NULL DEFAULT 1 COMMENT '0 上传中 1 可用 2 异常' AFTER download_count;

ALTER TABLE resource_files
    ADD CONSTRAINT chk_resource_files_object_status
        CHECK (object_status IN (0, 1, 2));

CREATE INDEX idx_resource_files_object_status_created
    ON resource_files (object_status, created_at);

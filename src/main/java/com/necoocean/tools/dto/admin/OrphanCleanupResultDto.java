package com.necoocean.tools.dto.admin;

/**
 * C-33 孤儿清理结果。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class OrphanCleanupResultDto {

    private final Integer deletedObjects;

    private final Integer deletedPendingRows;

    /**
     * @param deletedObjects     删除的 COS 孤儿对象数
     * @param deletedPendingRows 删除的超时上传占位行数
     */
    public OrphanCleanupResultDto(Integer deletedObjects, Integer deletedPendingRows) {
        this.deletedObjects = deletedObjects;
        this.deletedPendingRows = deletedPendingRows;
    }

    /**
     * @return 删除的对象数
     */
    public Integer getDeletedObjects() {
        return deletedObjects;
    }

    /**
     * @return 删除的占位行数
     */
    public Integer getDeletedPendingRows() {
        return deletedPendingRows;
    }
}

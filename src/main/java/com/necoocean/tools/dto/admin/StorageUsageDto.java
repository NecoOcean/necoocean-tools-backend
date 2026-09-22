package com.necoocean.tools.dto.admin;

import java.util.List;

/**
 * 工具存储占用。汇总库内 file_size，并带异常文件数。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class StorageUsageDto {

    private final Long totalBytes;
    private final Long fileCount;
    private final Long abnormalCount;
    private final List<StorageVersionUsageDto> byVersion;

    /**
     * @param totalBytes    总字节
     * @param fileCount     文件数
     * @param abnormalCount 异常文件数
     * @param byVersion     按版本分组
     */
    public StorageUsageDto(Long totalBytes, Long fileCount, Long abnormalCount,
            List<StorageVersionUsageDto> byVersion) {
        this.totalBytes = totalBytes;
        this.fileCount = fileCount;
        this.abnormalCount = abnormalCount;
        this.byVersion = byVersion == null ? List.of() : List.copyOf(byVersion);
    }

    /**
     * @return 总字节
     */
    public Long getTotalBytes() {
        return totalBytes;
    }

    /**
     * @return 文件数
     */
    public Long getFileCount() {
        return fileCount;
    }

    /**
     * @return 异常文件数
     */
    public Long getAbnormalCount() {
        return abnormalCount;
    }

    /**
     * @return 按版本分组
     */
    public List<StorageVersionUsageDto> getByVersion() {
        return byVersion;
    }

    @Override
    public String toString() {
        return "StorageUsageDto{totalBytes=" + totalBytes + '}';
    }
}

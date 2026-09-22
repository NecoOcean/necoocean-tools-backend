package com.necoocean.tools.dto.admin;

import java.util.List;

/**
 * 工具存储占用。只汇总库内 file_size，不查对象存储。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class StorageUsageDto {

    private final Long totalBytes;
    private final Long fileCount;
    private final List<StorageVersionUsageDto> byVersion;

    /**
     * @param totalBytes 总字节
     * @param fileCount  文件数
     * @param byVersion  按版本分组
     */
    public StorageUsageDto(Long totalBytes, Long fileCount, List<StorageVersionUsageDto> byVersion) {
        this.totalBytes = totalBytes;
        this.fileCount = fileCount;
        this.byVersion = byVersion == null ? List.of() : List.copyOf(byVersion);
    }

    /**
     * 读取字段。
     *
     * @return 值
     */
    public Long getTotalBytes() {
        return totalBytes;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public Long getFileCount() {
        return fileCount;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public List<StorageVersionUsageDto> getByVersion() {
        return byVersion;
    }
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "StorageUsageDto{totalBytes=" + totalBytes + '}';
    }
}
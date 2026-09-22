package com.necoocean.tools.dto.admin;

/**
 * 按版本汇总的存储占用。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class StorageVersionUsageDto {

    private final String version;
    private final Long bytes;
    private final Long fileCount;

    /**
     * @param version   版本号
     * @param bytes     字节数
     * @param fileCount 文件数
     */
    public StorageVersionUsageDto(String version, Long bytes, Long fileCount) {
        this.version = version;
        this.bytes = bytes;
        this.fileCount = fileCount;
    }

    /**
     * 读取字段。
     *
     * @return 值
     */
    public String getVersion() {
        return version;
    }
    /**
     * 读取字段。
     *
     * @return 值
     */
    public Long getBytes() {
        return bytes;
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
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "StorageVersionUsageDto{version=" + version + '}';
    }
}
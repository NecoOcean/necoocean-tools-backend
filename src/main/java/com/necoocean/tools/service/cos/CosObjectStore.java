package com.necoocean.tools.service.cos;

import java.time.Instant;
import java.util.List;

/**
 * 对象存储门面。上传预签名、下载预签名、存在性与清理。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public interface CosObjectStore {

    /**
     * 是否已可用于签名与读写。
     *
     * @return true 表示可用
     */
    boolean available();

    /**
     * 对象是否存在。
     *
     * @param objectKey 对象键
     * @return true 表示存在
     */
    boolean objectExists(String objectKey);

    /**
     * 签发预签名 PUT 地址。
     *
     * @param objectKey 对象键
     * @return 预签名 URL
     */
    String generateUploadUrl(String objectKey);

    /**
     * 签发预签名 GET 地址，强制附件下载。
     *
     * @param objectKey   对象键
     * @param displayName 下载文件名
     * @return 预签名 URL
     */
    String generateDownloadUrl(String objectKey, String displayName);

    /**
     * 删除单个对象。对象不存在时忽略。
     *
     * @param objectKey 对象键
     */
    void deleteObject(String objectKey);

    /**
     * 删除指定前缀下全部对象。
     *
     * @param prefix 前缀，如 {@code 12/} 或 {@code covers/12/}
     * @return 删除条数
     */
    int deleteByPrefix(String prefix);

    /**
     * 列出早于阈值的对象，供孤儿清理。
     *
     * @param olderThan 早于该时刻
     * @return 摘要列表
     */
    List<CosObjectSummary> listObjectsOlderThan(Instant olderThan);

    /**
     * 测试用：直接写入对象，不经预签名。默认不支持。
     *
     * @param objectKey 对象键
     * @param content   内容
     */
    default void putObjectForTest(String objectKey, byte[] content) {
        throw new UnsupportedOperationException("putObjectForTest unsupported");
    }

    /**
     * 关闭底层客户端。
     */
    void shutdown();
}

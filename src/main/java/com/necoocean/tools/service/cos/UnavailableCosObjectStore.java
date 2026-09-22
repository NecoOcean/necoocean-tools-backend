package com.necoocean.tools.service.cos;

import java.time.Instant;
import java.util.List;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.config.CosProperties;

/**
 * COS 未配置时的占位。上传与签名下载一律失败，存在性查询返回 false。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class UnavailableCosObjectStore implements CosObjectStore {

    private final CosProperties properties;

    /**
     * @param properties COS 配置
     */
    public UnavailableCosObjectStore(CosProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public boolean objectExists(String objectKey) {
        return false;
    }

    @Override
    public String generateUploadUrl(String objectKey) {
        throw new BizException(ErrorCode.INTERNAL_ERROR);
    }

    @Override
    public String generateDownloadUrl(String objectKey, String displayName) {
        throw new BizException(ErrorCode.OBJECT_MISSING);
    }

    @Override
    public void deleteObject(String objectKey) {
        // 未配置时无对象可删
    }

    @Override
    public int deleteByPrefix(String prefix) {
        return 0;
    }

    @Override
    public List<CosObjectSummary> listObjectsOlderThan(Instant olderThan) {
        return List.of();
    }

    @Override
    public void shutdown() {
        // 无客户端
    }

    /**
     * 便于日志对照。
     *
     * @return 配置摘要
     */
    public CosProperties getProperties() {
        return properties;
    }
}

package com.necoocean.tools.service.cos;

import java.time.Instant;

/**
 * 对象摘要。用于孤儿清理比对。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public final class CosObjectSummary {

    private final String objectKey;

    private final Instant lastModified;

    /**
     * @param objectKey    对象键
     * @param lastModified 最后修改时间
     */
    public CosObjectSummary(String objectKey, Instant lastModified) {
        this.objectKey = objectKey;
        this.lastModified = lastModified;
    }

    /**
     * 对象键。
     *
     * @return 对象键
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 最后修改时间。
     *
     * @return Instant
     */
    public Instant getLastModified() {
        return lastModified;
    }
}

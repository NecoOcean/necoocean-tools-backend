package com.necoocean.tools.service.cos;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.config.CosProperties;

/**
 * 进程内假对象存储。供自动化测试走完整上传 / 下载 / 清理路径。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MemoryCosObjectStore implements CosObjectStore {

    private static final String MEMORY_URL_PREFIX = "https://memory.local/";

    private final CosProperties properties;

    private final Map<String, StoredObject> objects = new ConcurrentHashMap<String, StoredObject>();

    /**
     * @param properties COS 配置
     */
    public MemoryCosObjectStore(CosProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public boolean objectExists(String objectKey) {
        return objects.containsKey(objectKey);
    }

    @Override
    public String generateUploadUrl(String objectKey) {
        requireKey(objectKey);
        return MEMORY_URL_PREFIX + "upload/" + objectKey + "?ttl=" + properties.getUploadTtlMinutes();
    }

    @Override
    public String generateDownloadUrl(String objectKey, String displayName) {
        requireKey(objectKey);
        if (!objects.containsKey(objectKey)) {
            throw new BizException(ErrorCode.OBJECT_MISSING);
        }
        return MEMORY_URL_PREFIX + "download/" + objectKey + "?name=" + displayName + "&ttl="
                + properties.getDownloadTtlMinutes();
    }

    @Override
    public void deleteObject(String objectKey) {
        if (objectKey != null) {
            objects.remove(objectKey);
        }
    }

    @Override
    public int deleteByPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return 0;
        }
        int deleted = 0;
        for (String key : new ArrayList<String>(objects.keySet())) {
            if (key.startsWith(prefix)) {
                objects.remove(key);
                deleted++;
            }
        }
        return deleted;
    }

    @Override
    public List<CosObjectSummary> listObjectsOlderThan(Instant olderThan) {
        List<CosObjectSummary> result = new ArrayList<CosObjectSummary>();
        for (Map.Entry<String, StoredObject> entry : objects.entrySet()) {
            if (entry.getValue().lastModified.isBefore(olderThan)) {
                result.add(new CosObjectSummary(entry.getKey(), entry.getValue().lastModified));
            }
        }
        return result;
    }

    @Override
    public void putObjectForTest(String objectKey, byte[] content) {
        requireKey(objectKey);
        byte[] body = content == null ? new byte[0] : content.clone();
        objects.put(objectKey, new StoredObject(body, Instant.now().minusSeconds(1L)));
    }

    /**
     * 写入带指定时间的对象，供孤儿清理测试。
     *
     * @param objectKey    对象键
     * @param content      内容
     * @param lastModified 最后修改时间
     */
    public void putObjectWithTime(String objectKey, byte[] content, Instant lastModified) {
        requireKey(objectKey);
        byte[] body = content == null ? new byte[0] : content.clone();
        Instant time = lastModified == null ? Instant.now() : lastModified;
        objects.put(objectKey, new StoredObject(body, time));
    }

    @Override
    public void shutdown() {
        objects.clear();
    }

    /**
     * 测试间清空。
     */
    public void clear() {
        objects.clear();
    }

    private static void requireKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
    }

    private static final class StoredObject {

        private final byte[] content;

        private final Instant lastModified;

        private StoredObject(byte[] content, Instant lastModified) {
            this.content = content;
            this.lastModified = lastModified;
        }
    }
}

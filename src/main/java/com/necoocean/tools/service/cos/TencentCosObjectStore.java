package com.necoocean.tools.service.cos;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.config.CosProperties;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.BucketCrossOriginConfiguration;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.CORSRule;
import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.ResponseHeaderOverrides;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 腾讯云 COS 实现。上传 60 分钟 PUT，下载 5 分钟 GET + 附件头。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class TencentCosObjectStore implements CosObjectStore {

    private static final Logger logger = LoggerFactory.getLogger(TencentCosObjectStore.class);

    private static final String OCTET_STREAM = "application/octet-stream";

    private static final int LIST_PAGE_SIZE = 1000;

    private static final int DELETE_BATCH_SIZE = 1000;

    private static final long MINUTES_TO_MILLIS = 60_000L;

    private final COSClient cosClient;

    private final CosProperties properties;

    /**
     * @param cosClient  已配置的客户端
     * @param properties 桶与时效
     */
    public TencentCosObjectStore(COSClient cosClient, CosProperties properties) {
        this.cosClient = cosClient;
        this.properties = properties;
        ensureBrowserCors();
    }

    /**
     * 为浏览器直传写入桶 CORS。缺配置时浏览器 PUT 会表现为 Failed to fetch。
     */
    public void ensureBrowserCors() {
        if (!properties.isCorsAutoApply()) {
            logger.info("COS CORS auto-apply disabled");
            return;
        }
        List<String> origins = properties.resolveCorsOrigins();
        if (origins.isEmpty()) {
            logger.warn("COS CORS origins empty; browser direct upload will fail with Failed to fetch");
            return;
        }
        try {
            CORSRule rule = new CORSRule();
            rule.setId("necoocean-browser-upload");
            rule.setAllowedOrigins(origins);
            rule.setAllowedMethods(Arrays.asList(
                    CORSRule.AllowedMethods.PUT,
                    CORSRule.AllowedMethods.GET,
                    CORSRule.AllowedMethods.HEAD,
                    CORSRule.AllowedMethods.POST));
            rule.setAllowedHeaders(Collections.singletonList("*"));
            rule.setExposedHeaders(Arrays.asList("ETag", "Content-Length", "x-cos-request-id"));
            rule.setMaxAgeSeconds(3600);

            BucketCrossOriginConfiguration configuration = new BucketCrossOriginConfiguration();
            configuration.setRules(Collections.singletonList(rule));
            cosClient.setBucketCrossOriginConfiguration(properties.getBucket(), configuration);
            logger.info("COS bucket CORS applied, bucket={}, origins={}", properties.getBucket(), origins);
        } catch (CosClientException exception) {
            logger.error(
                    "Failed to apply COS bucket CORS (browser upload will show Failed to fetch). "
                            + "Grant PutBucketCORS to the sub-user or set CORS manually in console. origins={}",
                    origins, exception);
        }
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public boolean objectExists(String objectKey) {
        try {
            return cosClient.doesObjectExist(properties.getBucket(), objectKey);
        } catch (CosClientException exception) {
            logger.warn("COS doesObjectExist failed, key={}", objectKey, exception);
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public String generateUploadUrl(String objectKey) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(properties.getBucket(), objectKey,
                HttpMethodName.PUT);
        request.setExpiration(expireAfterMinutes(properties.getUploadTtlMinutes()));
        request.setContentType(OCTET_STREAM);
        return toUrl(request, objectKey);
    }

    @Override
    public String generateDownloadUrl(String objectKey, String displayName) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(properties.getBucket(), objectKey,
                HttpMethodName.GET);
        request.setExpiration(expireAfterMinutes(properties.getDownloadTtlMinutes()));
        ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
        overrides.setContentType(OCTET_STREAM);
        overrides.setContentDisposition(attachmentDisposition(displayName));
        request.setResponseHeaders(overrides);
        return toUrl(request, objectKey);
    }

    @Override
    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            cosClient.deleteObject(properties.getBucket(), objectKey);
        } catch (CosClientException exception) {
            logger.warn("COS deleteObject failed, key={}", objectKey, exception);
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public int deleteByPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return 0;
        }
        int deleted = 0;
        String marker = null;
        do {
            ObjectListing listing = listPage(prefix, marker);
            List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<DeleteObjectsRequest.KeyVersion>();
            for (COSObjectSummary summary : listing.getObjectSummaries()) {
                keys.add(new DeleteObjectsRequest.KeyVersion(summary.getKey()));
                if (keys.size() >= DELETE_BATCH_SIZE) {
                    deleted += deleteBatch(keys);
                    keys.clear();
                }
            }
            if (!keys.isEmpty()) {
                deleted += deleteBatch(keys);
            }
            marker = listing.isTruncated() ? listing.getNextMarker() : null;
        } while (marker != null);
        return deleted;
    }

    @Override
    public List<CosObjectSummary> listObjectsOlderThan(Instant olderThan) {
        List<CosObjectSummary> result = new ArrayList<CosObjectSummary>();
        String marker = null;
        do {
            ObjectListing listing = listPage(null, marker);
            for (COSObjectSummary summary : listing.getObjectSummaries()) {
                Date lastModified = summary.getLastModified();
                if (lastModified != null && lastModified.toInstant().isBefore(olderThan)) {
                    result.add(new CosObjectSummary(summary.getKey(), lastModified.toInstant()));
                }
            }
            marker = listing.isTruncated() ? listing.getNextMarker() : null;
        } while (marker != null);
        return result;
    }

    @Override
    public void shutdown() {
        cosClient.shutdown();
    }

    private ObjectListing listPage(String prefix, String marker) {
        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(properties.getBucket());
        request.setMaxKeys(LIST_PAGE_SIZE);
        if (prefix != null) {
            request.setPrefix(prefix);
        }
        if (marker != null) {
            request.setMarker(marker);
        }
        try {
            return cosClient.listObjects(request);
        } catch (CosClientException exception) {
            logger.warn("COS listObjects failed, prefix={}", prefix, exception);
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private int deleteBatch(List<DeleteObjectsRequest.KeyVersion> keys) {
        DeleteObjectsRequest request = new DeleteObjectsRequest(properties.getBucket());
        request.setKeys(keys);
        try {
            cosClient.deleteObjects(request);
            return keys.size();
        } catch (CosClientException exception) {
            logger.warn("COS deleteObjects failed, size={}", keys.size(), exception);
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String toUrl(GeneratePresignedUrlRequest request, String objectKey) {
        try {
            URL url = cosClient.generatePresignedUrl(request);
            return url.toString();
        } catch (CosClientException exception) {
            logger.warn("COS generatePresignedUrl failed, key={}", objectKey, exception);
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private static Date expireAfterMinutes(int minutes) {
        long ttlMillis = minutes * MINUTES_TO_MILLIS;
        return new Date(System.currentTimeMillis() + ttlMillis);
    }

    private static String attachmentDisposition(String displayName) {
        String safeName = displayName == null || displayName.isBlank() ? "download" : displayName.trim();
        try {
            String encoded = URLEncoder.encode(safeName, StandardCharsets.UTF_8.name()).replace("+", "%20");
            return "attachment; filename*=UTF-8''" + encoded;
        } catch (UnsupportedEncodingException exception) {
            return "attachment; filename=\"download\"";
        }
    }
}

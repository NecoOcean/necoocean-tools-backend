package com.necoocean.tools.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 腾讯云 COS 配置。密钥只从环境变量或 config/local.yml 注入，不进仓库。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@ConfigurationProperties(prefix = "tools.cos")
public class CosProperties {

    /** 进程内假存储，供自动化测试。 */
    public static final String PROVIDER_MEMORY = "memory";

    /** 真实腾讯云 COS。 */
    public static final String PROVIDER_TENCENT = "tencent";

    private String provider = PROVIDER_TENCENT;

    private String region = "";

    private String bucket = "";

    private String secretId = "";

    private String secretKey = "";

    private int uploadTtlMinutes = 60;

    private int downloadTtlMinutes = 5;

    private int orphanAgeHours = 24;

    /**
     * 浏览器直传允许的 Origin，逗号分隔。须含本地 Vite 与生产站点来源。
     */
    private String corsOrigins = "http://127.0.0.1:5173,http://localhost:5173,http://127.0.0.1:4173,http://localhost:4173";

    /**
     * 启动时是否自动把 CORS 规则写入桶（需子用户具备 PutBucketCORS）。
     */
    private boolean corsAutoApply = true;

    /**
     * 存储实现。memory 或 tencent。
     *
     * @return 实现名
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @param provider 实现名
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 地域，如 ap-shanghai。
     *
     * @return 地域
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region 地域
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * 桶名。
     *
     * @return 桶名
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * @param bucket 桶名
     */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * SecretId。
     *
     * @return SecretId
     */
    public String getSecretId() {
        return secretId;
    }

    /**
     * @param secretId SecretId
     */
    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    /**
     * SecretKey。
     *
     * @return SecretKey
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * @param secretKey SecretKey
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * 上传预签名有效期（分钟）。
     *
     * @return 分钟
     */
    public int getUploadTtlMinutes() {
        return uploadTtlMinutes;
    }

    /**
     * @param uploadTtlMinutes 分钟
     */
    public void setUploadTtlMinutes(int uploadTtlMinutes) {
        this.uploadTtlMinutes = uploadTtlMinutes;
    }

    /**
     * 下载预签名有效期（分钟）。
     *
     * @return 分钟
     */
    public int getDownloadTtlMinutes() {
        return downloadTtlMinutes;
    }

    /**
     * @param downloadTtlMinutes 分钟
     */
    public void setDownloadTtlMinutes(int downloadTtlMinutes) {
        this.downloadTtlMinutes = downloadTtlMinutes;
    }

    /**
     * 孤儿对象判定窗口（小时）。
     *
     * @return 小时
     */
    public int getOrphanAgeHours() {
        return orphanAgeHours;
    }

    /**
     * @param orphanAgeHours 小时
     */
    public void setOrphanAgeHours(int orphanAgeHours) {
        this.orphanAgeHours = orphanAgeHours;
    }

    /**
     * 浏览器直传 CORS 允许的 Origin（逗号分隔原文）。
     *
     * @return 配置原文
     */
    public String getCorsOrigins() {
        return corsOrigins;
    }

    /**
     * @param corsOrigins 逗号分隔 Origin
     */
    public void setCorsOrigins(String corsOrigins) {
        this.corsOrigins = corsOrigins;
    }

    /**
     * 是否启动时自动写入桶 CORS。
     *
     * @return true 表示自动写入
     */
    public boolean isCorsAutoApply() {
        return corsAutoApply;
    }

    /**
     * @param corsAutoApply 是否自动写入
     */
    public void setCorsAutoApply(boolean corsAutoApply) {
        this.corsAutoApply = corsAutoApply;
    }

    /**
     * 解析 CORS Origin 列表（去空白、去重，保序）。
     *
     * @return Origin 列表
     */
    public java.util.List<String> resolveCorsOrigins() {
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<String>();
        if (corsOrigins != null) {
            for (String part : corsOrigins.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    set.add(trimmed);
                }
            }
        }
        return new java.util.ArrayList<String>(set);
    }

    /**
     * 腾讯云模式是否已填齐必要配置。
     *
     * @return true 表示可创建真实客户端
     */
    public boolean isTencentReady() {
        return PROVIDER_TENCENT.equalsIgnoreCase(provider) && hasText(region) && hasText(bucket) && hasText(secretId)
                && hasText(secretKey);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

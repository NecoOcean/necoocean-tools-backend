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

package com.necoocean.tools.config;

import com.necoocean.tools.service.cos.CosObjectStore;
import com.necoocean.tools.service.cos.MemoryCosObjectStore;
import com.necoocean.tools.service.cos.TencentCosObjectStore;
import com.necoocean.tools.service.cos.UnavailableCosObjectStore;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * COS 客户端与对象存储门面。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Configuration
@EnableConfigurationProperties(CosProperties.class)
public class CosConfig {

    private static final Logger logger = LoggerFactory.getLogger(CosConfig.class);

    /**
     * 按配置选择内存假存储、腾讯云或不可用占位。
     *
     * @param properties COS 配置
     * @return 对象存储门面
     */
    @Bean(destroyMethod = "shutdown")
    public CosObjectStore cosObjectStore(CosProperties properties) {
        if (CosProperties.PROVIDER_MEMORY.equalsIgnoreCase(properties.getProvider())) {
            logger.info("COS provider=memory");
            return new MemoryCosObjectStore(properties);
        }
        if (properties.isTencentReady()) {
            COSCredentials credentials = new BasicCOSCredentials(properties.getSecretId(), properties.getSecretKey());
            ClientConfig clientConfig = new ClientConfig(new Region(properties.getRegion()));
            clientConfig.setHttpProtocol(HttpProtocol.https);
            COSClient client = new COSClient(credentials, clientConfig);
            logger.info("COS provider=tencent, region={}, bucket={}", properties.getRegion(), properties.getBucket());
            return new TencentCosObjectStore(client, properties);
        }
        logger.warn("COS not configured; upload and signed download unavailable");
        return new UnavailableCosObjectStore(properties);
    }
}

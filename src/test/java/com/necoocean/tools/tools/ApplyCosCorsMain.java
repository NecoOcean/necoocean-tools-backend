package com.necoocean.tools.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.BucketCrossOriginConfiguration;
import com.qcloud.cos.model.CORSRule;
import com.qcloud.cos.region.Region;

/**
 * 一次性：给桶写入浏览器直传 CORS。用法见注释，不进入正式包。
 */
public final class ApplyCosCorsMain {

    private ApplyCosCorsMain() {
    }

    public static void main(String[] args) {
        String region = env("COS_REGION");
        String bucket = env("COS_BUCKET");
        String secretId = env("COS_SECRET_ID");
        String secretKey = env("COS_SECRET_KEY");
        String originsRaw = System.getenv().getOrDefault("COS_CORS_ORIGINS",
                "http://127.0.0.1:5173,http://localhost:5173,http://127.0.0.1:4173,http://localhost:4173");
        List<String> origins = Arrays.stream(originsRaw.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .toList();

        COSClient client = new COSClient(new BasicCOSCredentials(secretId, secretKey),
                new ClientConfig(new Region(region)) {{
                    setHttpProtocol(HttpProtocol.https);
                }});
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
            client.setBucketCrossOriginConfiguration(bucket, configuration);
            System.out.println("OK CORS applied to " + bucket + " origins=" + origins);
        } finally {
            client.shutdown();
        }
    }

    private static String env(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("missing env " + key);
        }
        return value;
    }
}

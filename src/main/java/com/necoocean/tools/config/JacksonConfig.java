package com.necoocean.tools.config;

import java.time.ZoneId;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JSON 契约。字段使用蛇形命名，保留 null，时间输出为带偏移的 ISO 8601。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
@Configuration
public class JacksonConfig {

    private static final String SHANGHAI_ZONE_ID = "Asia/Shanghai";

    /**
     * 定制全局 ObjectMapper。
     *
     * @return Jackson 构建定制器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            builder.timeZone(TimeZone.getTimeZone(ZoneId.of(SHANGHAI_ZONE_ID)));
            builder.serializationInclusion(JsonInclude.Include.ALWAYS);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.modules(new JavaTimeModule());
        };
    }
}

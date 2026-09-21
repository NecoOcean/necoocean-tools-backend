package com.necoocean.tools.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 装配。真实 IP 的取数规则在 {@link com.necoocean.tools.common.ClientIpResolver}。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 未声明 Accept 时按 JSON 响应。
     *
     * @param configurer 内容协商配置
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorParameter(false).defaultContentType(MediaType.APPLICATION_JSON);
    }
}

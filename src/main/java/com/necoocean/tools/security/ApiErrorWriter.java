package com.necoocean.tools.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.common.ErrorCode;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * 把安全过滤器里的拒绝写成 JSON 信封，避免落到 Spring Security 的 HTML 错误页。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Component
public class ApiErrorWriter {

    private final ObjectMapper objectMapper;

    /**
     * @param objectMapper 与接口层共用的序列化器
     */
    public ApiErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 写入标准错误信封。响应已提交时不再覆盖。
     *
     * @param response  当前响应
     * @param errorCode 错误码
     * @throws IOException 写出失败
     */
    public void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(errorCode.getHttpStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(errorCode));
    }
}

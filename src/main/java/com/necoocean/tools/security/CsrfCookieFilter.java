package com.necoocean.tools.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 迫使 CSRF Cookie 在本次响应里写出。双提交需要浏览器先拿到 csrf_token。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Component
public class CsrfCookieFilter extends OncePerRequestFilter {

    /**
     * 读取令牌，触发 Cookie 写入。
     *
     * @param request     当前请求
     * @param response    当前响应
     * @param filterChain 过滤器链
     * @throws ServletException 过滤器链抛出的 servlet 异常
     * @throws IOException      过滤器链抛出的 IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}

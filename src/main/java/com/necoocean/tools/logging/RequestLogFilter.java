package com.necoocean.tools.logging;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.necoocean.tools.common.ClientIpResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 为每个请求生成 traceId，并在访问日志里记录方法、路径、状态、耗时和脱敏后的 IP。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLogFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLogFilter.class);

    /**
     * 写入追踪号并记录访问日志。查询串不进入日志。
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
        long startNanos = System.nanoTime();
        String traceId = resolveTraceId(request.getHeader(TraceConstants.RESPONSE_HEADER));
        MDC.put(TraceConstants.MDC_KEY, traceId);
        try {
            response.setHeader(TraceConstants.RESPONSE_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            try {
                logAccess(request, response, startNanos);
            } finally {
                MDC.remove(TraceConstants.MDC_KEY);
            }
        }
    }

    /**
     * 只接受 8 到 64 位十六进制追踪号，其余情况在服务端重新生成。
     *
     * @param headerValue 请求头原值，可以为 null
     * @return 安全的追踪号
     */
    static String resolveTraceId(String headerValue) {
        if (!isAcceptableTraceId(headerValue)) {
            return newTraceId();
        }
        return headerValue.trim();
    }

    private static boolean isAcceptableTraceId(String headerValue) {
        if (headerValue == null) {
            return false;
        }
        String candidate = headerValue.trim();
        int length = candidate.length();
        if (length < TraceConstants.MIN_LENGTH || length > TraceConstants.MAX_LENGTH) {
            return false;
        }
        for (int index = 0; index < length; index++) {
            if (!isHex(candidate.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHex(char character) {
        boolean digit = character >= '0' && character <= '9';
        boolean lower = character >= 'a' && character <= 'f';
        boolean upper = character >= 'A' && character <= 'F';
        return digit || lower || upper;
    }

    private static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static void logAccess(HttpServletRequest request, HttpServletResponse response, long startNanos) {
        long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        String clientIp = LogMasker.maskIp(ClientIpResolver.resolve(request));
        logger.info("http request, method={}, path={}, status={}, costMs={}, clientIp={}",
                LogMasker.sanitize(request.getMethod()),
                LogMasker.sanitize(request.getRequestURI()),
                response.getStatus(),
                costMs,
                clientIp);
    }
}

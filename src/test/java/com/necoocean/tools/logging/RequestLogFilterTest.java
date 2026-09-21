package com.necoocean.tools.logging;

import java.util.concurrent.atomic.AtomicReference;

import com.necoocean.tools.common.ClientIpResolver;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 追踪号贯穿一次请求，并在结束后从 MDC 清除。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
class RequestLogFilterTest {

    private final RequestLogFilter filter = new RequestLogFilter();

    @Test
    void keepsHexTraceIdAndMasksClientIp() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public/health");
        request.addHeader(TraceConstants.RESPONSE_HEADER, "0123abcd0123abcd");
        request.addHeader(ClientIpResolver.FORWARDED_FOR_HEADER, "203.0.113.8, 10.0.0.2");
        request.setQueryString("email=neco@example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ListAppender<ILoggingEvent> appender = attachAppender();
        try {
            filter.doFilter(request, response, (servletRequest, servletResponse) ->
                    ((HttpServletResponse) servletResponse).setStatus(201));
            assertThat(response.getHeader(TraceConstants.RESPONSE_HEADER)).isEqualTo("0123abcd0123abcd");
            assertThat(MDC.get(TraceConstants.MDC_KEY)).isNull();
            String message = appender.list.get(0).getFormattedMessage();
            assertThat(message).contains("status=201");
            assertThat(message).contains("203.0.*.*");
            assertThat(message).doesNotContain("203.0.113.8");
            assertThat(message).doesNotContain("neco@example.com");
            assertThat(message).doesNotContain("10.0.0.2");
        } finally {
            detachAppender(appender);
        }
    }

    @Test
    void replacesUnsafeTraceIdAndClearsMdcWhenChainFails() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public/health");
        request.addHeader(TraceConstants.RESPONSE_HEADER, "0123456789abcdef\nINJECT");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<String>();
        assertThatThrownBy(() -> filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            seen.set(MDC.get(TraceConstants.MDC_KEY));
            throw new ServletException("boom");
        })).isInstanceOf(ServletException.class);
        assertThat(seen.get()).matches("[0-9a-f]{32}");
        assertThat(seen.get()).doesNotContain("INJECT");
        assertThat(response.getHeader(TraceConstants.RESPONSE_HEADER)).isEqualTo(seen.get());
        assertThat(MDC.get(TraceConstants.MDC_KEY)).isNull();
    }

    @Test
    void shortTraceIdIsRejected() {
        assertThat(RequestLogFilter.resolveTraceId("abc")).matches("[0-9a-f]{32}");
        assertThat(RequestLogFilter.resolveTraceId(null)).matches("[0-9a-f]{32}");
        assertThat(RequestLogFilter.resolveTraceId("DEADBEEFdeadbeef")).isEqualTo("DEADBEEFdeadbeef");
    }

    private static ListAppender<ILoggingEvent> attachAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLogFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private static void detachAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLogFilter.class);
        logger.detachAppender(appender);
    }
}

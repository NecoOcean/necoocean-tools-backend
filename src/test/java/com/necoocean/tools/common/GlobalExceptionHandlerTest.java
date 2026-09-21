package com.necoocean.tools.common;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 全局异常的分流：业务失败、参数失败和未知异常。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void bizExceptionKeepsStandardCopyAndWarnsWithStack() {
        ListAppender<ILoggingEvent> appender = attachAppender();
        try {
            BizException exception = new BizException(ErrorCode.TOOL_NOT_FOUND, new IllegalStateException("cause"));
            ResponseEntity<ApiResponse<Void>> response = handler.handleBizException(exception);
            assertThat(response.getStatusCode().value()).isEqualTo(404);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo(40401);
            assertThat(response.getBody().getMessage()).isEqualTo("该工具不存在或已下架。");
            ILoggingEvent event = appender.list.get(0);
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getThrowableProxy()).isNotNull();
        } finally {
            detachAppender(appender);
        }
    }

    @Test
    void unknownExceptionHidesInternalMessage() {
        ListAppender<ILoggingEvent> appender = attachAppender();
        try {
            IllegalStateException failure = new IllegalStateException("disk-secret");
            ResponseEntity<ApiResponse<Void>> response = handler.handleUnknown(failure);
            assertThat(response.getStatusCode().value()).isEqualTo(500);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo(50000);
            assertThat(response.getBody().getMessage()).doesNotContain("disk-secret");
            assertThat(response.getBody().toString()).doesNotContain("disk-secret");
            ILoggingEvent event = appender.list.get(0);
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getThrowableProxy().getMessage()).contains("disk-secret");
        } finally {
            detachAppender(appender);
        }
    }

    @Test
    void missingRouteDoesNotUseBusinessNotFoundCopy() {
        NoHandlerFoundException exception = new NoHandlerFoundException("GET", "/missing", new HttpHeaders());
        ResponseEntity<ApiResponse<Void>> response = handler.handleNoHandler(exception);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.ROUTE_NOT_FOUND.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo("请求的资源不存在。");
    }

    @Test
    void missingParameterUsesParamInvalid() throws Exception {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("page", "int");
        ResponseEntity<ApiResponse<Void>> response = handler.handleMissingParameter(exception);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.PARAM_INVALID.getCode());
    }

    @Test
    void unsupportedMethodUsesParamInvalid() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("TRACE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleMethodNotSupported(exception);
        assertThat(response.getStatusCode().value()).isEqualTo(422);
    }

    @Test
    @SuppressWarnings("unchecked")
    void constraintViolationLogsPathOnly() {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));
        ResponseEntity<ApiResponse<Void>> response = handler.handleConstraint(exception);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(42200);
    }

    @Test
    void bizExceptionWithoutCauseStillExposesErrorCode() {
        BizException exception = new BizException(ErrorCode.CSRF_INVALID);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CSRF_INVALID);
        assertThat(exception.getMessage()).isEqualTo("请求校验失败，请刷新页面后重试。");
    }

    private static ListAppender<ILoggingEvent> attachAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private static void detachAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logger.detachAppender(appender);
    }
}

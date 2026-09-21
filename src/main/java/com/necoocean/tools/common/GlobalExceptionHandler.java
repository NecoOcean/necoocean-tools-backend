package com.necoocean.tools.common;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常兜底。未知异常记 ERROR 和完整堆栈，响应只返回标准文案。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常。WARN 级别保留堆栈和 cause，不升到 ERROR。
     *
     * @param exception 业务异常
     * @return 与错误码对应的 JSON 信封
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBizException(BizException exception) {
        logger.warn("business exception, code={}", exception.getErrorCode().getCode(), exception);
        return response(exception.getErrorCode());
    }

    /**
     * 未匹配路由。只记路径，不把堆栈打成错误。
     *
     * @param exception 未匹配异常
     * @return HTTP 404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException exception) {
        logger.warn("no handler, method={}, path={}", exception.getHttpMethod(),
                sanitize(exception.getRequestURL()));
        return response(ErrorCode.ROUTE_NOT_FOUND);
    }

    /**
     * Spring 6 在静态资源链上对未命中路径抛出该异常。按 404 返回，不记成服务器错误。
     *
     * @param exception 资源未命中异常
     * @return HTTP 404
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException exception) {
        logger.warn("no resource, method={}, path={}", exception.getHttpMethod(),
                sanitize(exception.getResourcePath()));
        return response(ErrorCode.ROUTE_NOT_FOUND);
    }

    /**
     * JSON 无法解析。
     *
     * @param exception 解析异常
     * @return HTTP 422
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException exception) {
        logger.warn("unreadable request body, reason={}", exception.getClass().getSimpleName());
        return response(ErrorCode.PARAM_INVALID);
    }

    /**
     * 缺少必填查询参数。
     *
     * @param exception 缺参异常
     * @return HTTP 422
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException exception) {
        logger.warn("missing parameter, name={}", exception.getParameterName());
        return response(ErrorCode.PARAM_INVALID);
    }

    /**
     * 请求方法不支持。
     *
     * @param exception 方法不支持异常
     * @return HTTP 422，与参数错误码对齐
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {
        logger.warn("method not supported, method={}", exception.getMethod());
        return response(ErrorCode.PARAM_INVALID);
    }

    /**
     * 请求体字段校验失败。只记录字段名，不记录字段值。
     *
     * @param exception 校验异常
     * @return HTTP 422
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBody(MethodArgumentNotValidException exception) {
        logger.warn("validation failed, fields={}", fieldNames(exception.getBindingResult()));
        return response(ErrorCode.PARAM_INVALID);
    }

    /**
     * 约束校验失败。只记录属性路径。
     *
     * @param exception 约束异常
     * @return HTTP 422
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException exception) {
        logger.warn("constraint violation, fields={}", violationNames(exception.getConstraintViolations()));
        return response(ErrorCode.PARAM_INVALID);
    }

    /**
     * 未预期异常。对外隐藏 message，日志保留完整堆栈。
     *
     * @param exception 未预期异常
     * @return HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception exception) {
        logger.error("unhandled exception", exception);
        return response(ErrorCode.INTERNAL_ERROR);
    }

    private static ResponseEntity<ApiResponse<Void>> response(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(errorCode));
    }

    private static String fieldNames(BindingResult bindingResult) {
        StringBuilder names = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            appendName(names, fieldError.getField());
        }
        return names.toString();
    }

    private static String violationNames(Set<ConstraintViolation<?>> violations) {
        StringBuilder names = new StringBuilder();
        if (violations == null) {
            return "";
        }
        for (ConstraintViolation<?> violation : violations) {
            appendName(names, String.valueOf(violation.getPropertyPath()));
        }
        return names.toString();
    }

    private static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.replace('\r', '_').replace('\n', '_');
    }

    private static void appendName(StringBuilder names, String name) {
        if (names.length() > 0) {
            names.append(',');
        }
        names.append(name);
    }
}

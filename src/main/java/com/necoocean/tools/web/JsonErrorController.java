package com.necoocean.tools.web;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.logging.LogMasker;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 容器错误页。保证未进入 Controller 的失败也返回 JSON，而不是 HTML。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
@RestController
public class JsonErrorController {

    private static final Logger logger = LoggerFactory.getLogger(JsonErrorController.class);

    private static final String ERROR_PATH = "/error";

    /**
     * 按容器转入的状态码返回统一信封。
     *
     * @param request 当前请求
     * @return JSON 错误信封
     */
    @RequestMapping(path = ERROR_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> error(HttpServletRequest request) {
        int status = resolveStatus(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
        String path = LogMasker.sanitize(stringValue(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)));
        logError(status, path, request.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
        if (status == HttpStatus.NOT_FOUND.value()) {
            return response(ErrorCode.ROUTE_NOT_FOUND);
        }
        if (status >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return response(ErrorCode.INTERNAL_ERROR);
        }
        return response(ErrorCode.PARAM_INVALID);
    }

    private static void logError(int status, String path, Object exceptionAttribute) {
        if (exceptionAttribute instanceof Throwable && status >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            logger.error("container error, status={}, path={}", status, path, (Throwable) exceptionAttribute);
            return;
        }
        if (exceptionAttribute instanceof Throwable) {
            logger.warn("container error, status={}, path={}", status, path, (Throwable) exceptionAttribute);
            return;
        }
        logger.warn("container error, status={}, path={}", status, path);
    }

    private static ResponseEntity<ApiResponse<Void>> response(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(errorCode));
    }

    private static int resolveStatus(Object statusAttribute) {
        if (statusAttribute instanceof Integer) {
            return ((Integer) statusAttribute).intValue();
        }
        if (statusAttribute instanceof String) {
            return parseStatus((String) statusAttribute);
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private static int parseStatus(String rawStatus) {
        try {
            return Integer.parseInt(rawStatus);
        } catch (NumberFormatException exception) {
            logger.warn("invalid error status");
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}

package com.necoocean.tools.web;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.common.ErrorCode;

import jakarta.servlet.RequestDispatcher;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 容器错误入口始终返回 JSON 信封。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
class JsonErrorControllerTest {

    private final JsonErrorController controller = new JsonErrorController();

    @Test
    void notFoundUsesRouteCode() {
        MockHttpServletRequest request = request(404, "/missing");
        ResponseEntity<ApiResponse<Void>> response = controller.error(request);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.ROUTE_NOT_FOUND.getCode());
    }

    @Test
    void serverErrorHidesExceptionText() {
        MockHttpServletRequest request = request(500, "/api/v1/public/health");
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new IllegalStateException("disk-secret"));
        ResponseEntity<ApiResponse<Void>> response = controller.error(request);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());
        assertThat(response.getBody().getMessage()).doesNotContain("disk-secret");
    }

    @Test
    void clientErrorUsesParamInvalid() {
        MockHttpServletRequest request = request(405, "/api/v1/public/health");
        ResponseEntity<ApiResponse<Void>> response = controller.error(request);
        assertThat(response.getStatusCode().value()).isEqualTo(422);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.PARAM_INVALID.getCode());
    }

    @Test
    void invalidStatusTextBecomesServerError() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, "nope");
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/x\ny");
        ResponseEntity<ApiResponse<Void>> response = controller.error(request);
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void stringStatusIsParsed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, "404");
        ResponseEntity<ApiResponse<Void>> response = controller.error(request);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.ROUTE_NOT_FOUND.getCode());
    }

    private static MockHttpServletRequest request(int status, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, path);
        return request;
    }
}

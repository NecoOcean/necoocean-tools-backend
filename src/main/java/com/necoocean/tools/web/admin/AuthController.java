package com.necoocean.tools.web.admin;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.dto.admin.AdminSessionDto;
import com.necoocean.tools.dto.admin.LoginRequest;
import com.necoocean.tools.dto.admin.PasswordChangeRequest;
import com.necoocean.tools.security.AdminSessionService;
import com.necoocean.tools.security.LoginAttemptService;
import com.necoocean.tools.security.LoginOutcome;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台认证。登录成功后下发会话 Cookie；写操作另需 X-CSRF-Token。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AuthController {

    private final LoginAttemptService loginAttemptService;

    private final AdminSessionService adminSessionService;

    /**
     * @param loginAttemptService 登录与锁定
     * @param adminSessionService 会话
     */
    public AuthController(LoginAttemptService loginAttemptService, AdminSessionService adminSessionService) {
        this.loginAttemptService = loginAttemptService;
        this.adminSessionService = adminSessionService;
    }

    /**
     * 登录。账号不存在和口令错误都返回 40100。锁定期返回 42902。
     *
     * @param request      登录请求
     * @param httpRequest  当前请求
     * @param httpResponse 当前响应
     * @return 当前登录态
     */
    @PostMapping("/login")
    public ApiResponse<AdminSessionDto> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        LoginOutcome outcome = loginAttemptService.login(request.getUsername(), request.getPassword());
        if (outcome == LoginOutcome.LOCKED) {
            throw new BizException(ErrorCode.LOGIN_RATE_LIMITED);
        }
        if (outcome != LoginOutcome.SUCCESS) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        adminSessionService.establish(httpRequest, httpResponse, request.getUsername());
        return ApiResponse.success(adminSessionService.current(request.getUsername()));
    }

    /**
     * 登出。销毁会话。
     *
     * @param httpRequest  当前请求
     * @param httpResponse 当前响应
     * @return 空成功信封
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        adminSessionService.clear(httpRequest, httpResponse);
        return ApiResponse.success(null);
    }

    /**
     * 当前登录态。未登录由安全过滤器返回 40100。
     *
     * @param authentication 当前认证
     * @return 登录态
     */
    @GetMapping("/me")
    public ApiResponse<AdminSessionDto> me(Authentication authentication) {
        AdminSessionDto session = adminSessionService.current(authentication.getName());
        if (session == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.success(session);
    }

    /**
     * 修改口令。旧口令错误返回 40301。
     *
     * @param request        新旧口令
     * @param authentication 当前认证
     * @return 空成功信封
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request,
            Authentication authentication) {
        loginAttemptService.changePassword(authentication.getName(), request.getOldPassword(),
                request.getNewPassword());
        return ApiResponse.success(null);
    }
}

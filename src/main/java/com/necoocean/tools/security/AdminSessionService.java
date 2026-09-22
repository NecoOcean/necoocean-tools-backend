package com.necoocean.tools.security;

import java.util.List;

import com.necoocean.tools.domain.entity.AdminUser;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.repository.AdminUserRepository;
import com.necoocean.tools.dto.admin.AdminSessionDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

/**
 * 把登录态写入 Cookie 会话，并在登出时清掉会话 Cookie。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class AdminSessionService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    private final AdminUserRepository adminUserRepository;

    /**
     * @param adminUserRepository 管理员存储
     */
    public AdminSessionService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    /**
     * 建立 12 小时绝对有效期的会话。口令不放进安全上下文。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param username 已通过校验的账号
     */
    public void establish(HttpServletRequest request, HttpServletResponse response, String username) {
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                username, null, List.of(new SimpleGrantedAuthority(ROLE_ADMIN)));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(AuthConstants.LOGIN_AT, EntityTimestamps.now());
        }
    }

    /**
     * 销毁服务端会话，并让浏览器丢掉会话 Cookie。
     *
     * @param request  当前请求
     * @param response 当前响应
     */
    public void clear(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        ResponseCookie cookie = ResponseCookie.from(AuthConstants.SESSION_COOKIE, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 组装当前登录态。不返回口令哈希和失败次数。
     *
     * @param username 登录账号
     * @return 登录态；账号不存在时为 null
     */
    public AdminSessionDto current(String username) {
        AdminUser user = adminUserRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null;
        }
        return new AdminSessionDto(user.getUsername(), EntityTimestamps.toOffset(user.getLastLoginAt()));
    }
}

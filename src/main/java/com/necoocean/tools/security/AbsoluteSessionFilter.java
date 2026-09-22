package com.necoocean.tools.security;

import java.io.IOException;
import java.time.LocalDateTime;

import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.domain.entity.EntityTimestamps;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 会话从登录时刻起 12 小时失效，不因后续访问而顺延。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Component
public class AbsoluteSessionFilter extends OncePerRequestFilter {

    private final ApiErrorWriter apiErrorWriter;

    /**
     * @param apiErrorWriter JSON 错误写出
     */
    public AbsoluteSessionFilter(ApiErrorWriter apiErrorWriter) {
        this.apiErrorWriter = apiErrorWriter;
    }

    /**
     * 已登录且超过绝对有效期时销毁会话并返回 40100。
     *
     * @param request     当前请求
     * @param response    当前响应
     * @param filterChain 过滤器链
     * @throws ServletException 过滤器链抛出的 servlet 异常
     * @throws IOException      写出响应或过滤器链抛出的 IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isLoggedIn(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }
        HttpSession session = request.getSession(false);
        if (session == null || isExpired(session)) {
            if (session != null) {
                session.invalidate();
            }
            SecurityContextHolder.clearContext();
            apiErrorWriter.write(response, ErrorCode.UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean isExpired(HttpSession session) {
        Object loginAt = session.getAttribute(AuthConstants.LOGIN_AT);
        if (!(loginAt instanceof LocalDateTime)) {
            return true;
        }
        LocalDateTime deadline = ((LocalDateTime) loginAt).plusHours(AuthConstants.SESSION_HOURS);
        return !deadline.isAfter(EntityTimestamps.now());
    }
}

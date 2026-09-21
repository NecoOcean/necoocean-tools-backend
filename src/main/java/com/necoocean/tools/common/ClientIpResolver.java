package com.necoocean.tools.common;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Objects;

/**
 * 解析客户端 IP。应用只监听回环地址时，转发头只能由本机 Nginx 写入，因此取第一段。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
public final class ClientIpResolver {

    /** Nginx 透传的转发链请求头。 */
    public static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private ClientIpResolver() {
    }

    /**
     * 取客户端 IP。转发头缺失或第一段为空时，退回 remoteAddr。
     *
     * @param request 当前请求，不能为空
     * @return 客户端 IP；两者都没有时返回空串，不返回 null
     */
    public static String resolve(HttpServletRequest request) {
        Objects.requireNonNull(request, "request");
        String forwarded = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwarded != null && !forwarded.isBlank()) {
            String firstHop = forwarded.split(",")[0].trim();
            if (!firstHop.isEmpty()) {
                return firstHop;
            }
        }
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null) {
            return "";
        }
        return remoteAddr;
    }
}

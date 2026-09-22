package com.necoocean.tools.security;

/**
 * 认证会话常量。Cookie 名和请求头以附录 C 为准。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public final class AuthConstants {

    /** 会话 Cookie。HttpOnly，前端不读。 */
    public static final String SESSION_COOKIE = "SESSION";

    /** 双提交 CSRF Cookie。非 HttpOnly，供前端读取。 */
    public static final String CSRF_COOKIE = "csrf_token";

    /** 写请求回传 CSRF 的请求头。 */
    public static final String CSRF_HEADER = "X-CSRF-Token";

    /** 会话里记录的登录时间，用于 12 小时绝对过期。 */
    public static final String LOGIN_AT = "LOGIN_AT";

    /** 连续失败达到该次数后锁定。 */
    public static final int MAX_FAILURES = 5;

    /** 锁定分钟数。锁定期内正确口令也拒绝。 */
    public static final int LOCK_MINUTES = 15;

    /** 会话绝对有效小时数。不按最后访问时间顺延。 */
    public static final int SESSION_HOURS = 12;

    /** 新口令最短长度。对应强密码要求。 */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /** BCrypt 只取前 72 字节。 */
    public static final int MAX_PASSWORD_LENGTH = 72;

    private AuthConstants() {
    }
}

package com.necoocean.tools.web.admin;

import com.necoocean.tools.domain.entity.AdminUser;
import com.necoocean.tools.domain.repository.AdminUserRepository;
import com.necoocean.tools.security.AuthConstants;

import jakarta.servlet.http.Cookie;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 后台接口测试辅助。先 GET /me 取 csrf_token，再带 cookie 与 X-CSRF-Token；会话用 MockHttpSession。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public final class AdminAuthSupport {

    /** 测试管理员账号。 */
    public static final String USERNAME = "admin";

    /** 测试管理员口令。不写入仓库业务配置。 */
    public static final String PASSWORD = "correct-password";

    private AdminAuthSupport() {
    }

    /**
     * 写入测试管理员。
     *
     * @param adminUserRepository 管理员仓库
     * @param passwordEncoder     口令编码器
     */
    public static void insertAdmin(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        adminUserRepository.deleteAll();
        AdminUser admin = new AdminUser();
        admin.setUsername(USERNAME);
        admin.setPasswordHash(passwordEncoder.encode(PASSWORD));
        admin.setFailedAttempts(Integer.valueOf(0));
        adminUserRepository.saveAndFlush(admin);
    }

    /**
     * 登录并返回会话与 CSRF Cookie。
     *
     * @param mockMvc MockMvc
     * @return 已登录上下文
     * @throws Exception 请求失败
     */
    public static SessionContext login(MockMvc mockMvc) throws Exception {
        Cookie csrf = csrfCookie(mockMvc);
        MvcResult login = mockMvc.perform(post("/api/v1/admin/login")
                        .cookie(csrf)
                        .header(AuthConstants.CSRF_HEADER, csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        Cookie refreshed = login.getResponse().getCookie(AuthConstants.CSRF_COOKIE);
        if (refreshed != null) {
            csrf = refreshed;
        }
        return new SessionContext(session, csrf);
    }

    /**
     * 匿名 GET /me，取出 CSRF Cookie。
     *
     * @param mockMvc MockMvc
     * @return CSRF Cookie
     * @throws Exception 请求失败
     */
    public static Cookie csrfCookie(MockMvc mockMvc) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/me")).andReturn();
        return result.getResponse().getCookie(AuthConstants.CSRF_COOKIE);
    }

    /**
     * 给写请求补上会话与 CSRF。
     *
     * @param builder 请求构造器
     * @param context 登录上下文
     * @return 同一构造器
     */
    public static MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder,
            SessionContext context) {
        return builder.session(context.getSession())
                .cookie(context.getCsrf())
                .header(AuthConstants.CSRF_HEADER, context.getCsrf().getValue());
    }

    /**
     * 给读请求补上会话。
     *
     * @param builder 请求构造器
     * @param context 登录上下文
     * @return 同一构造器
     */
    public static MockHttpServletRequestBuilder withSession(MockHttpServletRequestBuilder builder,
            SessionContext context) {
        return builder.session(context.getSession());
    }

    private static String loginBody(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    /**
     * 已登录的会话与 CSRF。
     *
     * @author NecoOcean
     * @date 2026/09/22
     */
    public static final class SessionContext {

        private final MockHttpSession session;

        private final Cookie csrf;

        /**
         * @param session 会话
         * @param csrf    CSRF Cookie
         */
        public SessionContext(MockHttpSession session, Cookie csrf) {
            this.session = session;
            this.csrf = csrf;
        }

        /**
         * 会话。
         *
         * @return MockHttpSession
         */
        public MockHttpSession getSession() {
            return session;
        }

        /**
         * CSRF Cookie。
         *
         * @return Cookie
         */
        public Cookie getCsrf() {
            return csrf;
        }
    }
}

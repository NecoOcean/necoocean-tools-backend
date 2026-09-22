package com.necoocean.tools.web.admin;

import java.time.LocalDateTime;

import com.necoocean.tools.domain.entity.AdminUser;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.repository.AdminUserRepository;
import com.necoocean.tools.dto.admin.LoginRequest;
import com.necoocean.tools.dto.admin.PasswordChangeRequest;
import com.necoocean.tools.security.AuthConstants;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 登录、登出、CSRF、锁定和绝对会话过期。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthSessionWebTest {

    private static final String USERNAME = "admin";

    private static final String PASSWORD = "correct-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void insertAdmin() {
        adminUserRepository.deleteAll();
        AdminUser admin = new AdminUser();
        admin.setUsername(USERNAME);
        admin.setPasswordHash(passwordEncoder.encode(PASSWORD));
        admin.setFailedAttempts(Integer.valueOf(0));
        adminUserRepository.saveAndFlush(admin);
    }

    @Test
    void loginSetsSessionAndCsrfThenLogout() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(USERNAME);
        loginRequest.setPassword(PASSWORD);
        assertThat(loginRequest.toString()).doesNotContain(PASSWORD);

        MvcResult anonymous = mockMvc.perform(get("/api/v1/admin/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100))
                .andReturn();
        Cookie csrf = anonymous.getResponse().getCookie(AuthConstants.CSRF_COOKIE);
        assertThat(csrf).isNotNull();
        assertThat(csrf.isHttpOnly()).isFalse();
        assertThat(csrf.getSecure()).isTrue();
        assertThat(csrf.getPath()).isEqualTo("/");
        assertThat(csrf.getAttribute("SameSite")).isEqualTo("Lax");

        mockMvc.perform(post("/api/v1/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(USERNAME, PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40302));

        MvcResult login = mockMvc.perform(post("/api/v1/admin/login")
                        .cookie(csrf)
                        .header(AuthConstants.CSRF_HEADER, csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(USERNAME))
                .andExpect(jsonPath("$.data.last_login_at").exists())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute(AuthConstants.LOGIN_AT)).isInstanceOf(LocalDateTime.class);

        mockMvc.perform(get("/api/v1/admin/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(USERNAME));

        MvcResult logout = mockMvc.perform(post("/api/v1/admin/logout")
                        .session(session)
                        .cookie(csrf)
                        .header(AuthConstants.CSRF_HEADER, csrf.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        assertThat(logout.getResponse().getHeaders("Set-Cookie").toString())
                .contains("SESSION=", "Max-Age=0", "HttpOnly", "Secure", "SameSite=Lax");
        assertThat(session.isInvalid()).isTrue();

        mockMvc.perform(get("/api/v1/admin/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void fifthFailureLocksEvenWhenPasswordIsCorrect() throws Exception {
        Cookie csrf = csrfCookie();
        for (int attempt = 1; attempt < AuthConstants.MAX_FAILURES; attempt++) {
            mockMvc.perform(post("/api/v1/admin/login")
                            .cookie(csrf)
                            .header(AuthConstants.CSRF_HEADER, csrf.getValue())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginBody(USERNAME, "wrong-password")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(40100));
        }
        mockMvc.perform(post("/api/v1/admin/login")
                        .cookie(csrf)
                        .header(AuthConstants.CSRF_HEADER, csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(USERNAME, "wrong-password")))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(42902))
                .andExpect(jsonPath("$.message").value("登录失败次数过多，请 15 分钟后再试。"));
        mockMvc.perform(post("/api/v1/admin/login")
                        .cookie(csrf)
                        .header(AuthConstants.CSRF_HEADER, csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(USERNAME, PASSWORD)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(42902));
    }

    @Test
    void expiredLockAllowsTheNextCorrectPassword() throws Exception {
        AdminUser admin = adminUserRepository.findByUsername(USERNAME).orElseThrow();
        admin.setFailedAttempts(Integer.valueOf(AuthConstants.MAX_FAILURES));
        admin.setLastFailedAt(EntityTimestamps.now().minusMinutes(AuthConstants.LOCK_MINUTES + 1));
        adminUserRepository.saveAndFlush(admin);

        Cookie csrf = csrfCookie();
        mockMvc.perform(post("/api/v1/admin/login")
                        .cookie(csrf)
                        .header(AuthConstants.CSRF_HEADER, csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        AdminUser refreshed = adminUserRepository.findByUsername(USERNAME).orElseThrow();
        assertThat(refreshed.getFailedAttempts()).isZero();
        assertThat(refreshed.getLastFailedAt()).isNull();
    }

    @Test
    void unknownUserUsesTheSameUnauthorizedCode() throws Exception {
        Cookie csrf = csrfCookie();
        mockMvc.perform(post("/api/v1/admin/login")
                        .cookie(csrf)
                        .header(AuthConstants.CSRF_HEADER, csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("missing", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void passwordChangeRejectsTheOldPasswordAndAcceptsAStrongNewOne() throws Exception {
        PasswordChangeRequest hidden = new PasswordChangeRequest();
        hidden.setOldPassword(PASSWORD);
        hidden.setNewPassword("replacement-password");
        assertThat(hidden.toString()).doesNotContain(PASSWORD);

        Cookie csrf = csrfCookie();
        MvcResult login = login(csrf, PASSWORD);
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(put("/api/v1/admin/password")
                        .session(session)
                        .cookie(csrf)
                        .header(AuthConstants.CSRF_HEADER, csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"old_password\":\"wrong\",\"new_password\":\"replacement-password\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40301));

        mockMvc.perform(put("/api/v1/admin/password")
                        .session(session)
                        .cookie(csrf)
                        .header(AuthConstants.CSRF_HEADER, csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"old_password\":\"" + PASSWORD + "\",\"new_password\":\"short\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(42200));

        mockMvc.perform(put("/api/v1/admin/password")
                        .session(session)
                        .cookie(csrf)
                        .header(AuthConstants.CSRF_HEADER, csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"old_password\":\"" + PASSWORD + "\",\"new_password\":\"replacement-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Cookie nextCsrf = csrfCookie();
        login(nextCsrf, "replacement-password").getResponse();
    }

    @Test
    void sessionExpiresTwelveHoursAfterLogin() throws Exception {
        Cookie csrf = csrfCookie();
        MvcResult login = login(csrf, PASSWORD);
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        LocalDateTime expiredAt = EntityTimestamps.now().minusHours(AuthConstants.SESSION_HOURS);
        session.setAttribute(AuthConstants.LOGIN_AT, expiredAt);
        mockMvc.perform(get("/api/v1/admin/me").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    private Cookie csrfCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/me")).andReturn();
        return result.getResponse().getCookie(AuthConstants.CSRF_COOKIE);
    }

    private MvcResult login(Cookie csrf, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/admin/login")
                        .cookie(csrf)
                        .header(AuthConstants.CSRF_HEADER, csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(USERNAME, password)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String loginBody(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }
}

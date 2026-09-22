package com.necoocean.tools.security;

import com.necoocean.tools.common.ErrorCode;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Cookie 会话和双提交 CSRF。公开接口豁免 CSRF，后台写操作比对 csrf_token 与 X-CSRF-Token。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 后台接口需要登录态。登录接口本身放行，但仍走 CSRF。
     *
     * @param http                   安全配置
     * @param apiErrorWriter         JSON 错误写出
     * @param csrfCookieFilter       写出 CSRF Cookie
     * @param absoluteSessionFilter  绝对会话过期
     * @return 过滤器链
     * @throws Exception 安全配置失败
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiErrorWriter apiErrorWriter,
            CsrfCookieFilter csrfCookieFilter, AbsoluteSessionFilter absoluteSessionFilter) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieName(AuthConstants.CSRF_COOKIE);
        csrfTokenRepository.setHeaderName(AuthConstants.CSRF_HEADER);
        csrfTokenRepository.setCookiePath("/");
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie.secure(true).sameSite("Lax").httpOnly(false));
        CsrfTokenRequestAttributeHandler csrfTokenRequestHandler = new CsrfTokenRequestAttributeHandler();
        csrfTokenRequestHandler.setCsrfRequestAttributeName(null);
        http.csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(csrfTokenRequestHandler)
                        .ignoringRequestMatchers("/api/v1/public/**", "/error"))
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().changeSessionId())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                apiErrorWriter.write(response, ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) -> {
                            if (exception instanceof CsrfException) {
                                apiErrorWriter.write(response, ErrorCode.CSRF_INVALID);
                                return;
                            }
                            apiErrorWriter.write(response, ErrorCode.FORBIDDEN);
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/public/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/login").permitAll()
                        .requestMatchers("/api/v1/admin/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterAfter(csrfCookieFilter, CsrfFilter.class)
                .addFilterAfter(absoluteSessionFilter, SecurityContextHolderFilter.class);
        return http.build();
    }

    /**
     * 口令只存 BCrypt 哈希。
     *
     * @return 口令编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 占位，避免 Spring Boot 在启动日志里生成一个默认口令。登录不走这套加载。
     *
     * @return 不会被登录流程调用的加载器
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("unused");
        };
    }
}

package com.necoocean.tools.security;

import com.necoocean.tools.domain.entity.AdminUser;
import com.necoocean.tools.domain.repository.AdminUserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 库里还没有管理员时，用环境变量创建一次。已有账号时不改口令，也不把口令写进日志。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Component
public class AdminAccountInitializer implements ApplicationRunner {

    /** 账号名环境变量。空着时用默认账号。 */
    public static final String USERNAME_KEY = "ADMIN_USERNAME";

    /** 口令环境变量。未设置时不创建账号。 */
    public static final String PASSWORD_KEY = "ADMIN_PASSWORD";

    /** 未指定账号名时使用的登录名。 */
    public static final String DEFAULT_USERNAME = "admin";

    /** 与 admin_users.username 列长一致。 */
    private static final int USERNAME_MAX_LENGTH = 50;

    private static final Logger logger = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final AdminUserRepository adminUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final Environment environment;

    /**
     * @param adminUserRepository 管理员存储
     * @param passwordEncoder     口令哈希
     * @param environment         环境变量和启动参数
     */
    public AdminAccountInitializer(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder,
            Environment environment) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    /**
     * 没有管理员且提供了口令时插入一行。已有管理员则保持原哈希和失败次数。
     *
     * @param args 启动参数，账号口令从环境读取，不从这里取
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        if (adminUserRepository.count() > 0) {
            return;
        }
        String password = environment.getProperty(PASSWORD_KEY);
        if (!StringUtils.hasText(password)) {
            logger.info("admin account not created, {} is unset", PASSWORD_KEY);
            return;
        }
        String username = resolveUsername();
        ensureUsernameLength(username);
        ensurePasswordLength(password);
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setFailedAttempts(Integer.valueOf(AdminUser.DEFAULT_FAILED_ATTEMPTS));
        adminUserRepository.save(admin);
        logger.info("admin account created, username={}", username);
    }

    private String resolveUsername() {
        String username = environment.getProperty(USERNAME_KEY);
        if (!StringUtils.hasText(username)) {
            return DEFAULT_USERNAME;
        }
        return username;
    }

    private void ensureUsernameLength(String username) {
        if (username.length() > USERNAME_MAX_LENGTH) {
            throw new IllegalStateException(USERNAME_KEY + " length is above " + USERNAME_MAX_LENGTH);
        }
    }

    private void ensurePasswordLength(String password) {
        int length = password.length();
        if (length < AuthConstants.MIN_PASSWORD_LENGTH || length > AuthConstants.MAX_PASSWORD_LENGTH) {
            throw new IllegalStateException(PASSWORD_KEY + " length is outside "
                    + AuthConstants.MIN_PASSWORD_LENGTH + " to " + AuthConstants.MAX_PASSWORD_LENGTH);
        }
    }
}

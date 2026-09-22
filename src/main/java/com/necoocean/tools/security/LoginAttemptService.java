package com.necoocean.tools.security;

import java.time.LocalDateTime;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.domain.entity.AdminUser;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.repository.AdminUserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录锁定。连续失败 5 次后锁定 15 分钟，锁定期内正确口令也拒绝，期满后自动恢复。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class LoginAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

    private final AdminUserRepository adminUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final String dummyHash;

    /**
     * @param adminUserRepository 管理员存储
     * @param passwordEncoder     口令哈希
     */
    public LoginAttemptService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("unused-timing-pad");
    }

    /**
     * 校验口令并维护失败次数。方法正常返回后事务已提交，调用方再按结果抛业务异常。
     *
     * @param username    登录账号
     * @param rawPassword 明文口令
     * @return 登录结果
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginOutcome login(String username, String rawPassword) {
        AdminUser user = adminUserRepository.findByUsername(username).orElse(null);
        if (user == null) {
            passwordEncoder.matches(rawPassword, dummyHash);
            logger.warn("login failed, username={}", username);
            return LoginOutcome.BAD_CREDENTIALS;
        }
        LocalDateTime now = EntityTimestamps.now();
        if (isLocked(user, now)) {
            logger.warn("login locked, username={}", username);
            return LoginOutcome.LOCKED;
        }
        clearExpiredLock(user, now);
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            recordFailure(user, now);
            logger.warn("login failed, username={}", username);
            if (isLocked(user, now)) {
                return LoginOutcome.LOCKED;
            }
            return LoginOutcome.BAD_CREDENTIALS;
        }
        user.setFailedAttempts(Integer.valueOf(AdminUser.DEFAULT_FAILED_ATTEMPTS));
        user.setLastFailedAt(null);
        user.setLastLoginAt(now);
        adminUserRepository.save(user);
        return LoginOutcome.SUCCESS;
    }

    /**
     * 修改口令。旧口令不匹配时回滚，不改哈希。
     *
     * @param username    当前登录账号
     * @param oldPassword 旧口令
     * @param newPassword 新口令
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String username, String oldPassword, String newPassword) {
        AdminUser user = adminUserRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BizException(ErrorCode.PASSWORD_INCORRECT);
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        adminUserRepository.save(user);
    }

    private boolean isLocked(AdminUser user, LocalDateTime now) {
        int attempts = attemptsOf(user);
        if (attempts < AuthConstants.MAX_FAILURES || user.getLastFailedAt() == null) {
            return false;
        }
        return user.getLastFailedAt().plusMinutes(AuthConstants.LOCK_MINUTES).isAfter(now);
    }

    private void clearExpiredLock(AdminUser user, LocalDateTime now) {
        int attempts = attemptsOf(user);
        if (attempts < AuthConstants.MAX_FAILURES || user.getLastFailedAt() == null) {
            return;
        }
        if (!user.getLastFailedAt().plusMinutes(AuthConstants.LOCK_MINUTES).isAfter(now)) {
            user.setFailedAttempts(Integer.valueOf(AdminUser.DEFAULT_FAILED_ATTEMPTS));
            user.setLastFailedAt(null);
        }
    }

    private void recordFailure(AdminUser user, LocalDateTime now) {
        int attempts = attemptsOf(user) + 1;
        user.setFailedAttempts(Integer.valueOf(attempts));
        user.setLastFailedAt(now);
        adminUserRepository.save(user);
    }

    private int attemptsOf(AdminUser user) {
        if (user.getFailedAttempts() == null) {
            return AdminUser.DEFAULT_FAILED_ATTEMPTS;
        }
        return user.getFailedAttempts().intValue();
    }
}

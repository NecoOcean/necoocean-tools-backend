package com.necoocean.tools.security;

import java.util.List;

import com.necoocean.tools.domain.entity.AdminUser;
import com.necoocean.tools.domain.repository.AdminUserRepository;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 只在空库且提供口令时创建管理员，重启不覆盖已有口令。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
class AdminAccountInitializerTest {

    private static final String PASSWORD = "correct-password";

    private final AdminUserRepository adminUserRepository = mock(AdminUserRepository.class);

    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private final MockEnvironment environment = new MockEnvironment();

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private AdminAccountInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new AdminAccountInitializer(adminUserRepository, passwordEncoder, environment);
        Logger logger = (Logger) LoggerFactory.getLogger(AdminAccountInitializer.class);
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(AdminAccountInitializer.class);
        logger.detachAppender(appender);
    }

    @Test
    void keepsExistingAccountUntouched() throws Exception {
        when(adminUserRepository.count()).thenReturn(1L);
        environment.setProperty(AdminAccountInitializer.PASSWORD_KEY, PASSWORD);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(adminUserRepository, never()).save(any(AdminUser.class));
        verify(passwordEncoder, never()).encode(any());
        assertThat(messages()).noneMatch(message -> message.contains(PASSWORD));
    }

    @Test
    void skipsWhenPasswordIsUnset() throws Exception {
        when(adminUserRepository.count()).thenReturn(0L);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(adminUserRepository, never()).save(any(AdminUser.class));
        assertThat(messages()).anyMatch(message -> message.contains(AdminAccountInitializer.PASSWORD_KEY));
    }

    @Test
    void createsDefaultAdminOnce() throws Exception {
        when(adminUserRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(PASSWORD)).thenReturn("$2a$10$hashed-value");
        environment.setProperty(AdminAccountInitializer.PASSWORD_KEY, PASSWORD);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<AdminUser> saved = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserRepository).save(saved.capture());
        AdminUser admin = saved.getValue();
        assertThat(admin.getUsername()).isEqualTo(AdminAccountInitializer.DEFAULT_USERNAME);
        assertThat(admin.getPasswordHash()).isEqualTo("$2a$10$hashed-value");
        assertThat(admin.getFailedAttempts()).isZero();
        assertThat(admin.getLastFailedAt()).isNull();
        assertThat(admin.toString()).doesNotContain(PASSWORD).doesNotContain("$2a$");
        assertThat(messages()).anyMatch(message -> message.contains("username=admin"));
        assertThat(messages()).noneMatch(message -> message.contains(PASSWORD));
    }

    @Test
    void usesConfiguredUsername() throws Exception {
        when(adminUserRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(PASSWORD)).thenReturn("hash");
        environment.setProperty(AdminAccountInitializer.USERNAME_KEY, "neco");
        environment.setProperty(AdminAccountInitializer.PASSWORD_KEY, PASSWORD);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<AdminUser> saved = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserRepository).save(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("neco");
    }

    @Test
    void rejectsAShortPasswordWithoutSaving() {
        when(adminUserRepository.count()).thenReturn(0L);
        environment.setProperty(AdminAccountInitializer.PASSWORD_KEY, "short");

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(AdminAccountInitializer.PASSWORD_KEY);
        verify(adminUserRepository, never()).save(any(AdminUser.class));
        assertThat(messages()).noneMatch(message -> message.contains("short"));
    }

    @Test
    void rejectsAnOverlongUsernameWithoutSaving() {
        when(adminUserRepository.count()).thenReturn(0L);
        environment.setProperty(AdminAccountInitializer.USERNAME_KEY, "n".repeat(51));
        environment.setProperty(AdminAccountInitializer.PASSWORD_KEY, PASSWORD);

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(AdminAccountInitializer.USERNAME_KEY);
        verify(adminUserRepository, never()).save(any(AdminUser.class));
    }

    private List<String> messages() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }
}

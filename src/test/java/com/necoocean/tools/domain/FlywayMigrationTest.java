package com.necoocean.tools.domain;

import com.necoocean.tools.domain.entity.SiteSetting;
import com.necoocean.tools.domain.entity.ToolCategory;
import com.necoocean.tools.domain.repository.SiteSettingRepository;
import com.necoocean.tools.domain.repository.ToolCategoryRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 在 MySQL 8.4 上执行 Flyway，并核对种子数据和实体映射。没有 Docker 时跳过。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class FlywayMigrationTest {

    @Container
    @ServiceConnection
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("necoocean_tools")
            .withUrlParam("characterEncoding", "UTF-8")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_0900_ai_ci");

    @DynamicPropertySource
    static void flywayProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "com.necoocean.tools.config.MysqlToolsDialect");
        registry.add("spring.datasource.hikari.connection-init-sql", () -> "SET time_zone = '+08:00'");
    }

    @Autowired
    private ToolCategoryRepository categories;

    @Autowired
    private SiteSettingRepository settings;

    @Test
    void appliesSchemaAndSeed() {
        assertThat(categories.count()).isEqualTo(4);
        assertThat(categories.findByName(ToolCategory.FALLBACK_NAME)).get()
                .extracting(ToolCategory::getId)
                .isEqualTo(ToolCategory.FALLBACK_ID);
        assertThat(settings.count()).isEqualTo(9);
        assertThat(settings.findById(SiteSetting.MESSAGE_AUDIT_MODE)).get()
                .extracting(SiteSetting::getValue)
                .isEqualTo(SiteSetting.AUDIT_MODE_POST);
        assertThat(settings.findById(SiteSetting.MESSAGE_BOARD_ENABLED)).get()
                .extracting(SiteSetting::getValue)
                .isEqualTo(SiteSetting.BOARD_ON);
        assertThat(settings.findById(SiteSetting.SITE_NAME)).get()
                .extracting(SiteSetting::getValue)
                .isNull();
    }
}

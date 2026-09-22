package com.necoocean.tools.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 迁移脚本包含 PRD 第八章的表和一期就要建的审核索引。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
class MigrationScriptTest {

    @Test
    void schemaContainsEightTablesAndAuditIndex() throws IOException {
        String schema = Files.readString(Path.of("src/main/resources/db/migration/V1__init_schema.sql"));
        assertThat(schema).contains("CREATE TABLE tool_categories");
        assertThat(schema).contains("CREATE TABLE tools");
        assertThat(schema).contains("CREATE TABLE release_notes");
        assertThat(schema).contains("CREATE TABLE resource_files");
        assertThat(schema).contains("CREATE TABLE messages");
        assertThat(schema).contains("CREATE TABLE message_replies");
        assertThat(schema).contains("CREATE TABLE admin_users");
        assertThat(schema).contains("CREATE TABLE site_settings");
        assertThat(schema).contains("idx_messages_audit_created");
        assertThat(schema).contains("uk_resource_files_one_latest");

        String seed = Files.readString(Path.of("src/main/resources/db/migration/V2__seed_reference_data.sql"));
        assertThat(seed).contains("其他工具");
        assertThat(seed).contains("message_audit_mode");
        assertThat(seed).contains("'post'");

        String createDatabase = Files.readString(Path.of("src/main/resources/db/create_database.sql"));
        assertThat(createDatabase).contains("CREATE DATABASE IF NOT EXISTS necoocean_tools");
        assertThat(createDatabase).contains("utf8mb4");
        assertThat(createDatabase).contains("utf8mb4_0900_ai_ci");
    }
}

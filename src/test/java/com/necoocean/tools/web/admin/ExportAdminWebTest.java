package com.necoocean.tools.web.admin;

import java.nio.charset.StandardCharsets;

import com.necoocean.tools.domain.entity.ExportAuditLog;
import com.necoocean.tools.domain.entity.Message;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.entity.ToolCategory;
import com.necoocean.tools.domain.repository.AdminUserRepository;
import com.necoocean.tools.domain.repository.ExportAuditLogRepository;
import com.necoocean.tools.domain.repository.MessageRepository;
import com.necoocean.tools.domain.repository.ToolCategoryRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import com.necoocean.tools.web.admin.AdminAuthSupport.SessionContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * C-42 导出：默认不含邮箱，显式勾选才含，并写审计。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExportAdminWebTest {

    private static final String HEX_64 = "0123456789abcdef".repeat(4);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminUsers;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ToolCategoryRepository categories;

    @Autowired
    private ToolRepository tools;

    @Autowired
    private MessageRepository messages;

    @Autowired
    private ExportAuditLogRepository auditLogs;

    private SessionContext auth;

    @BeforeEach
    void setUp() throws Exception {
        auditLogs.deleteAll();
        messages.deleteAll();
        tools.deleteAll();
        categories.deleteAll();
        AdminAuthSupport.insertAdmin(adminUsers, passwordEncoder);
        auth = AdminAuthSupport.login(mockMvc);

        ToolCategory category = new ToolCategory();
        category.setName(ToolCategory.FALLBACK_NAME);
        category.setSortOrder(Integer.valueOf(4));
        categories.saveAndFlush(category);

        Tool tool = new Tool();
        tool.setName("导出工具");
        tool.setSlug("export-tool");
        tool.setSummary("导出冒烟");
        tool.setCategory(category);
        tool.setPlatforms("Windows");
        tool.setStatus(Integer.valueOf(Tool.STATUS_PUBLISHED));
        tools.saveAndFlush(tool);

        Message message = new Message();
        message.setTool(tool);
        message.setNickname("访客");
        message.setContactEmail("secret@example.com");
        message.setCategory(Integer.valueOf(Message.CATEGORY_SUGGESTION));
        message.setContent("希望导出");
        message.setAuditStatus(Integer.valueOf(Message.AUDIT_APPROVED));
        message.setIpHash(HEX_64);
        messages.saveAndFlush(message);
    }

    @Test
    void exportMessagesOmitsEmailByDefaultAndAudits() throws Exception {
        MvcResult result = mockMvc.perform(AdminAuthSupport.withSession(
                        get("/api/v1/admin/export").param("type", "messages").param("format", "json"), auth))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("export-messages.json")))
                .andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("希望导出");
        assertThat(body).doesNotContain("secret@example.com");
        assertThat(body).doesNotContain("contact_email");
        assertThat(body).doesNotContain("_warning");

        assertThat(auditLogs.findAll()).hasSize(1);
        ExportAuditLog log = auditLogs.findAll().get(0);
        assertThat(log.getOperator()).isEqualTo(AdminAuthSupport.USERNAME);
        assertThat(log.getExportType()).isEqualTo("messages");
        assertThat(log.getExportFormat()).isEqualTo("json");
        assertThat(log.getIncludeEmail()).isEqualTo(ExportAuditLog.EMAIL_EXCLUDED);
    }

    @Test
    void exportMessagesWithEmailRequiresExplicitTrue() throws Exception {
        MvcResult result = mockMvc.perform(AdminAuthSupport.withSession(get("/api/v1/admin/export")
                                .param("type", "messages")
                                .param("format", "csv")
                                .param("include_email", "true"), auth))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
                .andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("contact_email");
        assertThat(body).contains("secret@example.com");
        assertThat(body).contains("警告");

        ExportAuditLog log = auditLogs.findAll().get(0);
        assertThat(log.getIncludeEmail()).isEqualTo(ExportAuditLog.EMAIL_INCLUDED);
    }

    @Test
    void exportRejectsUnknownType() throws Exception {
        mockMvc.perform(AdminAuthSupport.withSession(
                        get("/api/v1/admin/export").param("type", "unknown").param("format", "json"), auth))
                .andExpect(status().isUnprocessableEntity());
    }
}

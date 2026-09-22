package com.necoocean.tools.web.admin;

import com.necoocean.tools.domain.entity.Message;
import com.necoocean.tools.domain.entity.SiteSetting;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.entity.ToolCategory;
import com.necoocean.tools.domain.repository.AdminUserRepository;
import com.necoocean.tools.domain.repository.MessageReplyRepository;
import com.necoocean.tools.domain.repository.MessageRepository;
import com.necoocean.tools.domain.repository.SiteSettingRepository;
import com.necoocean.tools.domain.repository.ToolCategoryRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import com.necoocean.tools.web.admin.AdminAuthSupport.SessionContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 留言管理与站点设置后台接口。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@SpringBootTest
@AutoConfigureMockMvc
class MessageSiteAdminWebTest {

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
    private MessageReplyRepository replies;

    @Autowired
    private SiteSettingRepository settings;

    private SessionContext auth;

    private Integer toolId;

    private Integer pendingId;

    private Integer approvedId;

    @BeforeEach
    void setUp() throws Exception {
        replies.deleteAll();
        messages.deleteAll();
        tools.deleteAll();
        categories.deleteAll();
        settings.deleteAll();
        AdminAuthSupport.insertAdmin(adminUsers, passwordEncoder);
        auth = AdminAuthSupport.login(mockMvc);

        ToolCategory category = new ToolCategory();
        category.setName("桌面工具");
        category.setSortOrder(Integer.valueOf(1));
        categories.saveAndFlush(category);

        Tool tool = new Tool();
        tool.setName("示例工具");
        tool.setSlug("demo-tool");
        tool.setSummary("简介");
        tool.setCategory(category);
        tool.setPlatforms("Windows");
        tool.setStatus(Integer.valueOf(Tool.STATUS_PUBLISHED));
        toolId = tools.saveAndFlush(tool).getId();

        Message pending = new Message();
        pending.setTool(tool);
        pending.setNickname("待审");
        pending.setContactEmail("hidden@example.com");
        pending.setCategory(Integer.valueOf(Message.CATEGORY_BUG));
        pending.setContent("待审留言");
        pending.setOsPlatform("Windows 11");
        pending.setToolVersion("1.0.0");
        pending.setReproSteps("步骤");
        pending.setAuditStatus(Integer.valueOf(Message.AUDIT_PENDING));
        pending.setKeywordFlagged(Integer.valueOf(Message.KEYWORD_FLAGGED));
        pending.setIpHash(HEX_64);
        pendingId = messages.saveAndFlush(pending).getId();

        Message approved = new Message();
        approved.setTool(tool);
        approved.setNickname("已通过");
        approved.setCategory(Integer.valueOf(Message.CATEGORY_SUGGESTION));
        approved.setContent("已通过留言");
        approved.setAuditStatus(Integer.valueOf(Message.AUDIT_APPROVED));
        approved.setIpHash(HEX_64);
        approvedId = messages.saveAndFlush(approved).getId();

        SiteSetting board = new SiteSetting();
        board.setKey(SiteSetting.MESSAGE_BOARD_ENABLED);
        board.setValue(SiteSetting.BOARD_ON);
        settings.saveAndFlush(board);
        SiteSetting name = new SiteSetting();
        name.setKey(SiteSetting.SITE_NAME);
        name.setValue("旧标题");
        settings.saveAndFlush(name);
    }

    @Test
    void messageListHidesEmailDetailShowsAndAuditRejectsRepeat() throws Exception {
        mockMvc.perform(AdminAuthSupport.withSession(get("/api/v1/admin/messages"), auth)
                        .param("has_email", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].has_email").value(true))
                .andExpect(jsonPath("$.data.list[0].contact_email").doesNotExist())
                .andExpect(jsonPath("$.data.list[0].keyword_flagged").value(true));

        mockMvc.perform(AdminAuthSupport.withSession(get("/api/v1/admin/messages/" + pendingId), auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contact_email").value("hidden@example.com"))
                .andExpect(jsonPath("$.data.os_platform").value("Windows 11"));

        MvcResult reply = mockMvc.perform(AdminAuthSupport.withAuth(
                        post("/api/v1/admin/messages/" + pendingId + "/replies"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"已收到\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Integer replyId = Integer.valueOf(com.jayway.jsonpath.JsonPath.read(reply.getResponse().getContentAsString(),
                "$.data.id").toString());

        mockMvc.perform(AdminAuthSupport.withAuth(patch("/api/v1/admin/messages/" + pendingId + "/status"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(AdminAuthSupport.withAuth(patch("/api/v1/admin/messages/" + pendingId + "/pin"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"is_pinned\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(AdminAuthSupport.withAuth(patch("/api/v1/admin/messages/" + pendingId + "/audit"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"audit_status\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(AdminAuthSupport.withAuth(patch("/api/v1/admin/messages/" + pendingId + "/audit"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"audit_status\":3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40908));

        mockMvc.perform(AdminAuthSupport.withAuth(patch("/api/v1/admin/messages/" + approvedId + "/audit"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"audit_status\":3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40908));

        Message anotherPending = new Message();
        anotherPending.setTool(tools.findById(toolId).orElseThrow());
        anotherPending.setNickname("批量");
        anotherPending.setCategory(Integer.valueOf(Message.CATEGORY_QUESTION));
        anotherPending.setContent("批量待审");
        anotherPending.setAuditStatus(Integer.valueOf(Message.AUDIT_PENDING));
        anotherPending.setIpHash(HEX_64);
        Integer batchId = messages.saveAndFlush(anotherPending).getId();

        mockMvc.perform(AdminAuthSupport.withAuth(post("/api/v1/admin/messages/audit-batch"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[" + batchId + "],\"audit_status\":3}"))
                .andExpect(status().isOk());

        Message third = new Message();
        third.setTool(tools.findById(toolId).orElseThrow());
        third.setNickname("一键");
        third.setCategory(Integer.valueOf(Message.CATEGORY_EXPERIENCE));
        third.setContent("一键待审");
        third.setAuditStatus(Integer.valueOf(Message.AUDIT_PENDING));
        third.setIpHash(HEX_64);
        messages.saveAndFlush(third);

        mockMvc.perform(AdminAuthSupport.withAuth(post("/api/v1/admin/messages/audit-all-pending"), auth))
                .andExpect(status().isOk());
        assertThat(messages.findByAuditStatus(Integer.valueOf(Message.AUDIT_PENDING))).isEmpty();

        mockMvc.perform(AdminAuthSupport.withSession(get("/api/v1/admin/messages/stats"), auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].message_count").value(4))
                .andExpect(jsonPath("$.data.list[0].bug_count").value(1))
                .andExpect(jsonPath("$.data.list[0].suggestion_count").value(1));

        mockMvc.perform(AdminAuthSupport.withAuth(delete("/api/v1/admin/replies/" + replyId), auth))
                .andExpect(status().isOk());
        mockMvc.perform(AdminAuthSupport.withAuth(delete("/api/v1/admin/messages/" + pendingId), auth))
                .andExpect(status().isOk());
        assertThat(messages.findById(pendingId)).isEmpty();
    }

    @Test
    void siteSettingsMapKeysAndKeepPollIntervalFixed() throws Exception {
        mockMvc.perform(AdminAuthSupport.withSession(get("/api/v1/admin/settings"), auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.site_title").value("旧标题"))
                .andExpect(jsonPath("$.data.message_enabled").value(true))
                .andExpect(jsonPath("$.data.message_poll_interval").value(30));

        mockMvc.perform(AdminAuthSupport.withAuth(put("/api/v1/admin/settings"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"site_title":"新标题","message_enabled":false,"message_keywords":"广告",
                                "message_audit_mode":"pre","home_intro":"介绍","copyright":"© 2026",
                                "icp_number":"桂ICP备1号","announcement":"公告","about_content":"关于",
                                "privacy_content":"隐私","message_poll_interval":999}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.site_title").value("新标题"))
                .andExpect(jsonPath("$.data.message_enabled").value(false))
                .andExpect(jsonPath("$.data.message_keywords").value("广告"))
                .andExpect(jsonPath("$.data.message_audit_mode").value("pre"))
                .andExpect(jsonPath("$.data.message_poll_interval").value(30))
                .andExpect(jsonPath("$.data.home_intro").value("介绍"));

        assertThat(settings.findById(SiteSetting.SITE_NAME).orElseThrow().getValue()).isEqualTo("新标题");
        assertThat(settings.findById(SiteSetting.MESSAGE_BOARD_ENABLED).orElseThrow().getValue())
                .isEqualTo(SiteSetting.BOARD_OFF);
        assertThat(settings.findById("message_poll_interval")).isEmpty();

        mockMvc.perform(AdminAuthSupport.withAuth(put("/api/v1/admin/settings"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"site_title\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.site_title").value(nullValue()));
    }
}

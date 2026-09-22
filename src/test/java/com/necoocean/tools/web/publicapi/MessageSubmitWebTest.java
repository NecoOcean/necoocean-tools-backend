package com.necoocean.tools.web.publicapi;

import com.necoocean.tools.domain.entity.Message;
import com.necoocean.tools.domain.entity.SiteSetting;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.entity.ToolCategory;
import com.necoocean.tools.domain.repository.MessageRepository;
import com.necoocean.tools.domain.repository.SiteSettingRepository;
import com.necoocean.tools.domain.repository.ToolCategoryRepository;
import com.necoocean.tools.domain.repository.ToolRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 留言提交先发后审，关键词只标记。公网 IP 受 3 条/工具与 5 条/IP 的 60 秒限制。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@SpringBootTest
@AutoConfigureMockMvc
class MessageSubmitWebTest {

    private static final String PUBLIC_IP = "203.0.113.10";

    private static final String BODY = """
            {"nickname":"访客","category":1,"content":"用起来顺手","contact_email":"user@example.com"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ToolCategoryRepository categories;

    @Autowired
    private ToolRepository tools;

    @Autowired
    private MessageRepository messages;

    @Autowired
    private SiteSettingRepository settings;

    @BeforeEach
    void insertPublishedTool() {
        messages.deleteAll();
        tools.deleteAll();
        categories.deleteAll();
        settings.deleteAll();

        ToolCategory category = new ToolCategory();
        category.setName("桌面工具");
        category.setSortOrder(Integer.valueOf(1));
        categories.saveAndFlush(category);

        saveTool(category, "示例工具", "demo-tool", Tool.STATUS_PUBLISHED);
        saveTool(category, "另一工具", "other-tool", Tool.STATUS_PUBLISHED);
        saveTool(category, "未上架", "hidden-tool", Tool.STATUS_UNPUBLISHED);
        saveSetting(SiteSetting.MESSAGE_BOARD_ENABLED, SiteSetting.BOARD_ON);
        saveSetting(SiteSetting.MESSAGE_AUDIT_MODE, SiteSetting.AUDIT_MODE_POST);
    }

    @Test
    void submitsWithoutCsrfAndPublishesImmediately() throws Exception {
        mockMvc.perform(post("/api/v1/public/tools/demo-tool/messages")
                        .header("X-Forwarded-For", PUBLIC_IP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.audit_status").value(2))
                .andExpect(jsonPath("$.data.audit_status_text").value("已通过"))
                .andExpect(jsonPath("$.data.keyword_flagged").value(false))
                .andExpect(jsonPath("$.data.content").doesNotExist())
                .andExpect(jsonPath("$.data.contact_email").doesNotExist());

        mockMvc.perform(get("/api/v1/public/tools/demo-tool/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].content").value("用起来顺手"))
                .andExpect(jsonPath("$.data.list[0].contact_email").doesNotExist());
    }

    @Test
    void flagsKeywordWithoutHidingTheMessage() throws Exception {
        saveSetting(SiteSetting.MESSAGE_KEYWORDS, "违禁样例");

        mockMvc.perform(post("/api/v1/public/tools/demo-tool/messages")
                        .header("X-Forwarded-For", PUBLIC_IP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"category":3,"content":"这里有违禁样例","os_platform":"Windows 11",
                                "tool_version":"1.0.0","repro_steps":"打开页面"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.audit_status").value(2))
                .andExpect(jsonPath("$.data.keyword_flagged").value(true));

        Message saved = messages.findAll().get(0);
        assertThat(saved.getNickname()).isEqualTo("匿名");
        assertThat(saved.getKeywordFlagged()).isEqualTo(Integer.valueOf(Message.KEYWORD_FLAGGED));
        assertThat(saved.getContactEmail()).isNull();
        assertThat(saved.getIpHash()).hasSize(64);
        mockMvc.perform(get("/api/v1/public/tools/demo-tool/messages"))
                .andExpect(jsonPath("$.data.list[0].content").value("这里有违禁样例"));
    }

    @Test
    void dropsBugFieldsForOtherCategories() throws Exception {
        mockMvc.perform(post("/api/v1/public/tools/demo-tool/messages")
                        .header("X-Forwarded-For", "203.0.113.11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"访客","category":2,"content":"希望加导出",
                                "os_platform":"Windows","tool_version":"9","repro_steps":"无"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/public/tools/demo-tool/messages"))
                .andExpect(jsonPath("$.data.list[0].os_platform").value(nullValue()))
                .andExpect(jsonPath("$.data.list[0].tool_version").value(nullValue()))
                .andExpect(jsonPath("$.data.list[0].repro_steps").value(nullValue()));
    }

    @Test
    void limitsThreePerToolAndFivePerIp() throws Exception {
        for (int index = 0; index < 3; index++) {
            submit("demo-tool", "第" + index + "条").andExpect(status().isOk());
        }
        submit("demo-tool", "超出单工具").andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(42901));
        assertThat(messages.count()).isEqualTo(3);

        submit("other-tool", "跨工具甲").andExpect(status().isOk());
        submit("other-tool", "跨工具乙").andExpect(status().isOk());
        submit("other-tool", "超出总配额").andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(42901));
        assertThat(messages.count()).isEqualTo(5);
    }

    @Test
    void doesNotLimitLoopbackAddress() throws Exception {
        for (int index = 0; index < 6; index++) {
            mockMvc.perform(post("/api/v1/public/tools/demo-tool/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body("本机第" + index + "条")))
                    .andExpect(status().isOk());
        }
        assertThat(messages.count()).isEqualTo(6);
    }

    @Test
    void rejectsClosedBoardAndBadInputWithoutSaving() throws Exception {
        updateSetting(SiteSetting.MESSAGE_BOARD_ENABLED, SiteSetting.BOARD_OFF);
        submit("demo-tool", "关闭后").andExpect(status().isConflict()).andExpect(jsonPath("$.code").value(40904));

        updateSetting(SiteSetting.MESSAGE_BOARD_ENABLED, SiteSetting.BOARD_ON);
        mockMvc.perform(post("/api/v1/public/tools/hidden-tool/messages")
                        .header("X-Forwarded-For", PUBLIC_IP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("下架")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
        mockMvc.perform(post("/api/v1/public/tools/demo-tool/messages")
                        .header("X-Forwarded-For", PUBLIC_IP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"访客\",\"category\":1,\"content\":\"好\","
                                + "\"contact_email\":\"not-an-email\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(42201));
        mockMvc.perform(post("/api/v1/public/tools/demo-tool/messages")
                        .header("X-Forwarded-For", PUBLIC_IP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + "名".repeat(51) + "\",\"category\":1,\"content\":\"好\"}"))
                .andExpect(jsonPath("$.code").value(42202));
        assertThat(messages.count()).isZero();
    }

    @Test
    void keepsPendingMessagesOffThePublicList() throws Exception {
        updateSetting(SiteSetting.MESSAGE_AUDIT_MODE, SiteSetting.AUDIT_MODE_PRE);

        submit("demo-tool", "待审").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.audit_status").value(1))
                .andExpect(jsonPath("$.data.audit_status_text").value("待审核"));
        Integer id = messages.findAll().get(0).getId();

        mockMvc.perform(get("/api/v1/public/tools/demo-tool/messages"))
                .andExpect(jsonPath("$.data.list").isEmpty());
        mockMvc.perform(get("/api/v1/public/messages/" + id + "/status"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.audit_status").value(1))
                .andExpect(jsonPath("$.data.content").doesNotExist())
                .andExpect(jsonPath("$.data.contact_email").doesNotExist())
                .andExpect(jsonPath("$.data.ip_hash").doesNotExist());
        mockMvc.perform(get("/api/v1/public/messages/999999/status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40402));
    }

    private ResultActions submit(String slug, String content) throws Exception {
        return mockMvc.perform(post("/api/v1/public/tools/" + slug + "/messages")
                .header("X-Forwarded-For", PUBLIC_IP)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(content)));
    }

    private static String body(String content) {
        return "{\"nickname\":\"访客\",\"category\":1,\"content\":\"" + content + "\"}";
    }

    private void saveTool(ToolCategory category, String name, String slug, int status) {
        Tool tool = new Tool();
        tool.setName(name);
        tool.setSlug(slug);
        tool.setSummary("简介");
        tool.setCategory(category);
        tool.setPlatforms("Windows");
        tool.setStatus(Integer.valueOf(status));
        tools.saveAndFlush(tool);
    }

    private void saveSetting(String key, String value) {
        SiteSetting setting = new SiteSetting();
        setting.setKey(key);
        setting.setValue(value);
        settings.saveAndFlush(setting);
    }

    private void updateSetting(String key, String value) {
        SiteSetting setting = settings.findById(key).orElseThrow();
        setting.setValue(value);
        settings.saveAndFlush(setting);
    }
}

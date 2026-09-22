package com.necoocean.tools.web.admin;

import java.time.LocalDate;

import com.necoocean.tools.domain.entity.Message;
import com.necoocean.tools.domain.entity.ReleaseNote;
import com.necoocean.tools.domain.entity.ResourceFile;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.entity.ToolCategory;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.repository.AdminUserRepository;
import com.necoocean.tools.domain.repository.MessageReplyRepository;
import com.necoocean.tools.domain.repository.MessageRepository;
import com.necoocean.tools.domain.repository.ReleaseNoteRepository;
import com.necoocean.tools.domain.repository.ResourceFileRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 工具、分类、更新日志与文件元信息后台接口。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@SpringBootTest
@AutoConfigureMockMvc
class ToolCategoryAdminWebTest {

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
    private ReleaseNoteRepository releaseNotes;

    @Autowired
    private ResourceFileRepository resourceFiles;

    @Autowired
    private MessageRepository messages;

    @Autowired
    private MessageReplyRepository replies;

    private SessionContext auth;

    private Integer fallbackCategoryId;

    private Integer customCategoryId;

    @BeforeEach
    void setUp() throws Exception {
        replies.deleteAll();
        messages.deleteAll();
        resourceFiles.deleteAll();
        releaseNotes.deleteAll();
        tools.deleteAll();
        categories.deleteAll();
        AdminAuthSupport.insertAdmin(adminUsers, passwordEncoder);
        auth = AdminAuthSupport.login(mockMvc);

        ToolCategory fallback = new ToolCategory();
        fallback.setName(ToolCategory.FALLBACK_NAME);
        fallback.setSortOrder(Integer.valueOf(4));
        fallbackCategoryId = categories.saveAndFlush(fallback).getId();

        ToolCategory custom = new ToolCategory();
        custom.setName("桌面工具");
        custom.setSortOrder(Integer.valueOf(1));
        customCategoryId = categories.saveAndFlush(custom).getId();
    }

    @Test
    void toolCrudStatusStorageAndCascadeDelete() throws Exception {
        MvcResult created = mockMvc.perform(AdminAuthSupport.withAuth(post("/api/v1/admin/tools"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"示例工具","slug":"demo-tool","summary":"简介",
                                "category_id":%d,"platforms":["Windows","Mac"],"developed_at":"2026-05-10",
                                "status":1}
                                """.formatted(customCategoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();
        Integer toolId = Integer.valueOf(com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(),
                "$.data.id").toString());

        mockMvc.perform(AdminAuthSupport.withAuth(post("/api/v1/admin/tools"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"重复","slug":"demo-tool","summary":"简介",
                                "category_id":%d,"platforms":["Windows"]}
                                """.formatted(customCategoryId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40902));

        mockMvc.perform(AdminAuthSupport.withAuth(put("/api/v1/admin/tools/" + toolId), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"示例工具","slug":"other-slug","summary":"简介",
                                "category_id":%d,"platforms":["Windows"]}
                                """.formatted(customCategoryId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40903));

        mockMvc.perform(AdminAuthSupport.withAuth(put("/api/v1/admin/tools/" + toolId), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"示例工具改","slug":"demo-tool","summary":"新简介",
                                "category_id":%d,"platforms":["Windows"],"developed_at":null}
                                """.formatted(customCategoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("示例工具改"))
                .andExpect(jsonPath("$.data.developed_at").value(nullValue()));

        mockMvc.perform(AdminAuthSupport.withAuth(patch("/api/v1/admin/tools/" + toolId + "/status"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk());

        mockMvc.perform(AdminAuthSupport.withSession(get("/api/v1/admin/tools"), auth)
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].status").value(0));

        Tool tool = tools.findById(toolId).orElseThrow();
        ReleaseNote note = new ReleaseNote();
        note.setTool(tool);
        note.setVersion("1.0.0");
        note.setContent("首发");
        note.setReleasedAt(EntityTimestamps.now());
        releaseNotes.saveAndFlush(note);

        ResourceFile file = new ResourceFile();
        file.setTool(tool);
        file.setVersion("1.0.0");
        file.setDisplayName("demo.exe");
        file.setObjectKey("1/1.0.0/a.exe");
        file.setExt("exe");
        file.setFileSize(Long.valueOf(2048L));
        file.setSha256(HEX_64);
        file.setPlatform("Windows");
        file.setLatest(Integer.valueOf(ResourceFile.NOT_LATEST));
        resourceFiles.saveAndFlush(file);

        Message message = new Message();
        message.setTool(tool);
        message.setNickname("访客");
        message.setCategory(Integer.valueOf(Message.CATEGORY_QUESTION));
        message.setContent("你好");
        message.setIpHash(HEX_64);
        messages.saveAndFlush(message);

        mockMvc.perform(AdminAuthSupport.withSession(get("/api/v1/admin/tools/" + toolId + "/storage-usage"), auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total_bytes").value(2048))
                .andExpect(jsonPath("$.data.file_count").value(1))
                .andExpect(jsonPath("$.data.by_version[0].version").value("1.0.0"));

        mockMvc.perform(AdminAuthSupport.withAuth(delete("/api/v1/admin/tools/" + toolId), auth))
                .andExpect(status().isOk());
        assertThat(tools.findById(toolId)).isEmpty();
        assertThat(releaseNotes.findAll()).isEmpty();
        assertThat(resourceFiles.findAll()).isEmpty();
        assertThat(messages.findAll()).isEmpty();
    }

    @Test
    void categoryRulesAndReleaseNoteVersionConflict() throws Exception {
        mockMvc.perform(AdminAuthSupport.withSession(get("/api/v1/admin/categories"), auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(2)));

        mockMvc.perform(AdminAuthSupport.withAuth(post("/api/v1/admin/categories"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"桌面工具\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40907));

        MvcResult created = mockMvc.perform(AdminAuthSupport.withAuth(post("/api/v1/admin/categories"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"脚本工具\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String body = created.getResponse().getContentAsString();
        Integer categoryId = Integer.valueOf(com.jayway.jsonpath.JsonPath.read(body,
                "$.data.id").toString());

        Tool tool = new Tool();
        tool.setName("占用");
        tool.setSlug("occupied");
        tool.setSummary("简介");
        tool.setCategory(categories.findById(categoryId).orElseThrow());
        tool.setPlatforms("Windows");
        tool.setStatus(Integer.valueOf(Tool.STATUS_PUBLISHED));
        tool.setDevelopedAt(LocalDate.of(2026, 1, 1));
        tools.saveAndFlush(tool);

        mockMvc.perform(AdminAuthSupport.withAuth(delete("/api/v1/admin/categories/" + categoryId), auth))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40905));

        mockMvc.perform(AdminAuthSupport.withAuth(delete("/api/v1/admin/categories/" + fallbackCategoryId), auth))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40906));

        mockMvc.perform(AdminAuthSupport.withAuth(post("/api/v1/admin/tools/" + tool.getId() + "/release-notes"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"1.0.0\",\"content\":\"首发\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(AdminAuthSupport.withAuth(post("/api/v1/admin/tools/" + tool.getId() + "/release-notes"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"1.0.0\",\"content\":\"重复\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40901));
    }

    @Test
    void releaseNoteListEditAndDelete() throws Exception {
        Tool tool = new Tool();
        tool.setName("日志工具");
        tool.setSlug("note-tool");
        tool.setSummary("简介");
        tool.setCategory(categories.findById(customCategoryId).orElseThrow());
        tool.setPlatforms("Windows");
        tool.setStatus(Integer.valueOf(Tool.STATUS_PUBLISHED));
        Integer toolId = tools.saveAndFlush(tool).getId();

        MvcResult created = mockMvc.perform(AdminAuthSupport.withAuth(
                        post("/api/v1/admin/tools/" + toolId + "/release-notes"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"1.0.0\",\"content\":\"首发\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Integer noteId = Integer.valueOf(com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(),
                "$.data.id").toString());

        mockMvc.perform(AdminAuthSupport.withSession(get("/api/v1/admin/tools/" + toolId + "/release-notes"), auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].version").value("1.0.0"));

        mockMvc.perform(AdminAuthSupport.withAuth(put("/api/v1/admin/release-notes/" + noteId), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"1.0.1\",\"content\":\"修订\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value("1.0.1"))
                .andExpect(jsonPath("$.data.content").value("修订"));

        mockMvc.perform(AdminAuthSupport.withAuth(delete("/api/v1/admin/release-notes/" + noteId), auth))
                .andExpect(status().isOk());
        assertThat(releaseNotes.findById(noteId)).isEmpty();
    }

    @Test
    void resourceFileLatestAndDeleteOnlyTouchesDatabase() throws Exception {
        Tool tool = new Tool();
        tool.setName("文件工具");
        tool.setSlug("file-tool");
        tool.setSummary("简介");
        tool.setCategory(categories.findById(customCategoryId).orElseThrow());
        tool.setPlatforms("Windows");
        tool.setStatus(Integer.valueOf(Tool.STATUS_PUBLISHED));
        tools.saveAndFlush(tool);

        ResourceFile first = new ResourceFile();
        first.setTool(tool);
        first.setVersion("1.0.0");
        first.setDisplayName("a.exe");
        first.setObjectKey("keep-object-key");
        first.setExt("exe");
        first.setFileSize(Long.valueOf(100L));
        first.setSha256(HEX_64);
        first.setPlatform("Windows");
        first.setLatest(Integer.valueOf(ResourceFile.NOT_LATEST));
        Integer firstId = resourceFiles.saveAndFlush(first).getId();

        ResourceFile second = new ResourceFile();
        second.setTool(tool);
        second.setVersion("2.0.0");
        second.setDisplayName("b.exe");
        second.setObjectKey("second-key");
        second.setExt("exe");
        second.setFileSize(Long.valueOf(200L));
        second.setSha256(HEX_64);
        second.setPlatform("Windows");
        second.setLatest(Integer.valueOf(ResourceFile.NOT_LATEST));
        Integer secondId = resourceFiles.saveAndFlush(second).getId();

        mockMvc.perform(AdminAuthSupport.withAuth(patch("/api/v1/admin/files/" + firstId + "/is-latest"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"is_latest\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.is_latest").value(true))
                .andExpect(jsonPath("$.data.object_key").value("keep-object-key"));
        assertThat(tools.findById(tool.getId()).orElseThrow().getLatestVersion()).isEqualTo("1.0.0");

        mockMvc.perform(AdminAuthSupport.withAuth(patch("/api/v1/admin/files/" + secondId + "/is-latest"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"is_latest\":true}"))
                .andExpect(status().isOk());
        assertThat(resourceFiles.findById(firstId).orElseThrow().getLatest())
                .isEqualTo(Integer.valueOf(ResourceFile.NOT_LATEST));
        assertThat(tools.findById(tool.getId()).orElseThrow().getLatestVersion()).isEqualTo("2.0.0");

        mockMvc.perform(AdminAuthSupport.withAuth(put("/api/v1/admin/files/" + secondId), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"display_name\":\"b2.exe\",\"platform\":\"Mac\",\"version\":\"2.1.0\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.display_name").value("b2.exe"))
                .andExpect(jsonPath("$.data.platform").value("Mac"));

        mockMvc.perform(AdminAuthSupport.withAuth(delete("/api/v1/admin/files/" + secondId), auth))
                .andExpect(status().isOk());
        assertThat(resourceFiles.findById(secondId)).isEmpty();
        assertThat(resourceFiles.findById(firstId).orElseThrow().getLatest())
                .isEqualTo(Integer.valueOf(ResourceFile.LATEST));
        assertThat(tools.findById(tool.getId()).orElseThrow().getLatestVersion()).isEqualTo("1.0.0");
    }
}

package com.necoocean.tools.web.publicapi;

import java.time.LocalDate;

import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.entity.Message;
import com.necoocean.tools.domain.entity.MessageReply;
import com.necoocean.tools.domain.entity.ReleaseNote;
import com.necoocean.tools.domain.entity.ResourceFile;
import com.necoocean.tools.domain.entity.SiteSetting;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.entity.ToolCategory;
import com.necoocean.tools.domain.repository.MessageReplyRepository;
import com.necoocean.tools.domain.repository.MessageRepository;
import com.necoocean.tools.domain.repository.ReleaseNoteRepository;
import com.necoocean.tools.domain.repository.ResourceFileRepository;
import com.necoocean.tools.domain.repository.SiteSettingRepository;
import com.necoocean.tools.domain.repository.ToolCategoryRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import com.necoocean.tools.service.cos.CosObjectStore;
import com.necoocean.tools.service.cos.MemoryCosObjectStore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 公开读只返回白名单字段。下架工具和未审核留言不可见。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@SpringBootTest
@AutoConfigureMockMvc
class PublicReadWebTest {

    private static final String HEX_64 = "0123456789abcdef".repeat(4);

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private SiteSettingRepository settings;

    @Autowired
    private CosObjectStore cosObjectStore;

    private Integer fileId;

    @BeforeEach
    void insertPublishedTool() {
        if (cosObjectStore instanceof MemoryCosObjectStore memory) {
            memory.clear();
        }
        replies.deleteAll();
        messages.deleteAll();
        resourceFiles.deleteAll();
        releaseNotes.deleteAll();
        tools.deleteAll();
        categories.deleteAll();
        settings.deleteAll();

        ToolCategory category = new ToolCategory();
        category.setName("桌面工具");
        category.setSortOrder(Integer.valueOf(1));
        categories.saveAndFlush(category);

        Tool hidden = new Tool();
        hidden.setName("未上架");
        hidden.setSlug("hidden-tool");
        hidden.setSummary("不可见");
        hidden.setCategory(category);
        hidden.setPlatforms("Windows");
        hidden.setStatus(Integer.valueOf(Tool.STATUS_UNPUBLISHED));
        tools.saveAndFlush(hidden);

        Tool tool = new Tool();
        tool.setName("示例工具");
        tool.setSlug("demo-tool");
        tool.setSummary("100% 完成");
        tool.setCoverObjectKey("covers/1/secret.png");
        tool.setCategory(category);
        tool.setPlatforms("Windows, Mac");
        tool.setTutorial("# 教程");
        tool.setRepoUrl("https://example.com/repo");
        tool.setWebUrl("https://example.com");
        tool.setLatestVersion("1.0.0");
        tool.setStatus(Integer.valueOf(Tool.STATUS_PUBLISHED));
        tool.setDevelopedAt(LocalDate.of(2026, 5, 10));
        tools.saveAndFlush(tool);

        Tool other = new Tool();
        other.setName("另一工具");
        other.setSlug("other-tool");
        other.setSummary("1000 行代码");
        other.setCategory(category);
        other.setPlatforms("通用");
        other.setStatus(Integer.valueOf(Tool.STATUS_PUBLISHED));
        tools.saveAndFlush(other);

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
        file.setObjectKey("1/1.0.0/secret.exe");
        file.setExt("exe");
        file.setFileSize(Long.valueOf(1024L));
        file.setSha256(HEX_64);
        file.setPlatform("Windows");
        file.setLatest(Integer.valueOf(ResourceFile.LATEST));
        fileId = resourceFiles.saveAndFlush(file).getId();

        Message bug = new Message();
        bug.setTool(tool);
        bug.setNickname("匿名");
        bug.setContactEmail("secret@example.com");
        bug.setCategory(Integer.valueOf(Message.CATEGORY_BUG));
        bug.setContent("打不开");
        bug.setOsPlatform("Windows 11");
        bug.setToolVersion("1.0.0");
        bug.setReproSteps("点击运行");
        bug.setPinned(Integer.valueOf(Message.PINNED));
        bug.setAuditStatus(Integer.valueOf(Message.AUDIT_APPROVED));
        bug.setIpHash(HEX_64);
        messages.saveAndFlush(bug);

        MessageReply reply = new MessageReply();
        reply.setMessage(bug);
        reply.setContent("已收到");
        replies.saveAndFlush(reply);

        Message pending = new Message();
        pending.setTool(tool);
        pending.setNickname("待审");
        pending.setCategory(Integer.valueOf(Message.CATEGORY_EXPERIENCE));
        pending.setContent("不该出现");
        pending.setOsPlatform("Linux");
        pending.setAuditStatus(Integer.valueOf(Message.AUDIT_PENDING));
        pending.setIpHash(HEX_64);
        messages.saveAndFlush(pending);

        SiteSetting title = new SiteSetting();
        title.setKey(SiteSetting.SITE_NAME);
        title.setValue("NecoOcean 工具集");
        settings.saveAndFlush(title);
        SiteSetting board = new SiteSetting();
        board.setKey(SiteSetting.MESSAGE_BOARD_ENABLED);
        board.setValue(SiteSetting.BOARD_ON);
        settings.saveAndFlush(board);
    }

    @Test
    void listsOnlyPublishedToolsAndKeepsCoverKeyPrivate() throws Exception {
        mockMvc.perform(get("/api/v1/public/tools").param("keyword", "100%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].slug").value("demo-tool"))
                .andExpect(jsonPath("$.data.list[0].platforms[0]").value("Windows"))
                .andExpect(jsonPath("$.data.list[0].platforms[1]").value("Mac"))
                .andExpect(jsonPath("$.data.list[0].cover_url").value(nullValue()))
                .andExpect(jsonPath("$.data.pagination.total").value(1));

        mockMvc.perform(get("/api/v1/public/tools").param("keyword", "k".repeat(51)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(42200));
    }

    @Test
    void detailNotesAndFilesOmitStorageKeys() throws Exception {
        mockMvc.perform(get("/api/v1/public/tools/hidden-tool"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));

        mockMvc.perform(get("/api/v1/public/tools/demo-tool"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tutorial").value("# 教程"))
                .andExpect(jsonPath("$.data.developed_at").value("2026-05-10"))
                .andExpect(jsonPath("$.data.cover_url").value(nullValue()));

        mockMvc.perform(get("/api/v1/public/tools/demo-tool/release-notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].version").value("1.0.0"));

        mockMvc.perform(get("/api/v1/public/tools/demo-tool/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].download_url").value("/download/" + fileId))
                .andExpect(jsonPath("$.data.list[0].is_latest").value(true))
                .andExpect(jsonPath("$.data.list[0].object_key").doesNotExist());
    }

    @Test
    void messagesStayOnTheApprovedWhitelist() throws Exception {
        mockMvc.perform(get("/api/v1/public/tools/demo-tool/messages"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].category_text").value("BUG报错"))
                .andExpect(jsonPath("$.data.list[0].os_platform").value("Windows 11"))
                .andExpect(jsonPath("$.data.list[0].is_pinned").value(true))
                .andExpect(jsonPath("$.data.list[0].replies[0].content").value("已收到"))
                .andExpect(jsonPath("$.data.list[0].contact_email").doesNotExist())
                .andExpect(jsonPath("$.data.list[0].ip_hash").doesNotExist())
                .andExpect(jsonPath("$.data.list[0].audit_status").doesNotExist());
    }

    @Test
    void siteInfoUsesFixedPollInterval() throws Exception {
        mockMvc.perform(get("/api/v1/public/site-info"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=60"))
                .andExpect(jsonPath("$.data.site_title").value("NecoOcean 工具集"))
                .andExpect(jsonPath("$.data.message_enabled").value(true))
                .andExpect(jsonPath("$.data.message_poll_interval").value(30));
    }

    @Test
    void downloadRedirectsWhenObjectExistsAndMarksMissing() throws Exception {
        mockMvc.perform(get("/download/" + fileId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40405));
        mockMvc.perform(get("/download/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40404));

        cosObjectStore.putObjectForTest("1/1.0.0/secret.exe", new byte[] {1});
        mockMvc.perform(get("/download/" + fileId))
                .andExpect(status().isFound())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Location", containsString("memory.local/download/")));
    }
}

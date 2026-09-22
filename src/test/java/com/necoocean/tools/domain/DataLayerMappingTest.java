package com.necoocean.tools.domain;

import java.time.LocalDate;

import com.necoocean.tools.config.MysqlToolsDialect;
import com.necoocean.tools.domain.entity.AdminUser;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.entity.Message;
import com.necoocean.tools.domain.entity.MessageReply;
import com.necoocean.tools.domain.entity.ReleaseNote;
import com.necoocean.tools.domain.entity.ResourceFile;
import com.necoocean.tools.domain.entity.SiteSetting;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.entity.ToolCategory;
import com.necoocean.tools.domain.repository.AdminUserRepository;
import com.necoocean.tools.domain.repository.MessageReplyRepository;
import com.necoocean.tools.domain.repository.MessageRepository;
import com.necoocean.tools.domain.repository.ReleaseNoteRepository;
import com.necoocean.tools.domain.repository.ResourceFileRepository;
import com.necoocean.tools.domain.repository.SiteSettingRepository;
import com.necoocean.tools.domain.repository.ToolCategoryRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 八张表的字段都能写入再读出。公开接口还不在这一层。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@DataJpaTest
class DataLayerMappingTest {

    private static final String HEX_64 = "0123456789abcdef".repeat(4);

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
    private AdminUserRepository admins;

    @Autowired
    private SiteSettingRepository settings;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndReadsEveryColumn() {
        assertThat(new MysqlToolsDialect().getDefaultTimestampPrecision()).isZero();
        assertThat(EntityTimestamps.now()).isNotNull();
        assertThat(SiteSetting.isDefinedKey(SiteSetting.MESSAGE_AUDIT_MODE)).isTrue();
        assertThat(SiteSetting.isDefinedKey("unknown")).isFalse();
        assertThat(SiteSetting.isDefinedKey(null)).isFalse();

        ToolCategory category = new ToolCategory();
        category.setName("桌面工具");
        category.setSortOrder(Integer.valueOf(3));
        categories.saveAndFlush(category);

        Tool tool = new Tool();
        tool.setName("示例工具");
        tool.setSlug("demo-tool");
        tool.setSummary("卡片简介");
        tool.setCoverObjectKey("covers/1/a.png");
        tool.setCategory(category);
        tool.setPlatforms("Windows,Mac");
        tool.setTutorial("# 教程");
        tool.setRepoUrl("https://example.com/repo");
        tool.setWebUrl("https://example.com");
        tool.setLatestVersion("1.0.0");
        tool.setStatus(Integer.valueOf(Tool.STATUS_PUBLISHED));
        tool.setDevelopedAt(LocalDate.of(2026, 9, 22));
        tool.setCreatedAt(null);
        tool.setUpdatedAt(null);
        tools.saveAndFlush(tool);

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
        file.setObjectKey("1/1.0.0/abc.exe");
        file.setExt("exe");
        file.setFileSize(Long.valueOf(1024L));
        file.setSha256(HEX_64);
        file.setPlatform("Windows");
        file.setLatest(Integer.valueOf(ResourceFile.LATEST));
        file.setDownloadCount(null);
        file.setCreatedAt(null);
        resourceFiles.saveAndFlush(file);

        Message message = new Message();
        message.setTool(tool);
        message.setNickname("匿名");
        message.setContactEmail("secret@example.com");
        message.setCategory(Integer.valueOf(Message.CATEGORY_BUG));
        message.setContent("打不开");
        message.setOsPlatform("Windows 11");
        message.setToolVersion("1.0.0");
        message.setReproSteps("点击运行");
        message.setStatus(null);
        message.setPinned(null);
        message.setAuditStatus(null);
        message.setKeywordFlagged(null);
        message.setAuditedAt(null);
        message.setIpHash(HEX_64);
        message.setCreatedAt(null);
        assertThat(message.toString()).doesNotContain("secret@example.com");
        messages.saveAndFlush(message);

        MessageReply reply = new MessageReply();
        reply.setMessage(message);
        reply.setContent("已收到");
        reply.setCreatedAt(null);
        replies.saveAndFlush(reply);

        AdminUser admin = new AdminUser();
        admin.setUsername("neco");
        admin.setPasswordHash("salted-hash");
        admin.setLastLoginAt(null);
        admin.setFailedAttempts(null);
        admin.setLastFailedAt(null);
        assertThat(admin.toString()).doesNotContain("salted-hash");
        admins.saveAndFlush(admin);

        SiteSetting setting = new SiteSetting();
        setting.setKey(SiteSetting.SITE_NAME);
        setting.setValue(null);
        setting.setUpdatedAt(null);
        settings.saveAndFlush(setting);

        entityManager.clear();

        Tool loaded = tools.findBySlug("demo-tool").orElseThrow();
        loaded.setId(loaded.getId());
        assertThat(loaded.getName()).isEqualTo("示例工具");
        assertThat(loaded.getSlug()).isEqualTo("demo-tool");
        assertThat(loaded.getSummary()).isEqualTo("卡片简介");
        assertThat(loaded.getCoverObjectKey()).isEqualTo("covers/1/a.png");
        assertThat(loaded.getCategory().getName()).isEqualTo("桌面工具");
        assertThat(loaded.getCategory().getSortOrder()).isEqualTo(3);
        assertThat(loaded.getPlatforms()).isEqualTo("Windows,Mac");
        assertThat(loaded.getTutorial()).isEqualTo("# 教程");
        assertThat(loaded.getRepoUrl()).isEqualTo("https://example.com/repo");
        assertThat(loaded.getWebUrl()).isEqualTo("https://example.com");
        assertThat(loaded.getLatestVersion()).isEqualTo("1.0.0");
        assertThat(loaded.getStatus()).isEqualTo(Tool.STATUS_PUBLISHED);
        assertThat(loaded.getDevelopedAt()).isEqualTo(LocalDate.of(2026, 9, 22));
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getUpdatedAt()).isNotNull();
        assertThat(loaded.toString()).contains("demo-tool");
        loaded.getCategory().setId(loaded.getCategory().getId());
        assertThat(loaded.getCategory().toString()).contains("桌面工具");

        loaded.setSummary("新简介");
        tools.saveAndFlush(loaded);
        entityManager.clear();
        assertThat(tools.findBySlug("demo-tool").orElseThrow().getSummary()).isEqualTo("新简介");

        ReleaseNote loadedNote = releaseNotes.findByToolIdOrderByReleasedAtDesc(loaded.getId()).get(0);
        loadedNote.setId(loadedNote.getId());
        assertThat(loadedNote.getTool().getId()).isEqualTo(loaded.getId());
        assertThat(loadedNote.getVersion()).isEqualTo("1.0.0");
        assertThat(loadedNote.getContent()).isEqualTo("首发");
        assertThat(loadedNote.getReleasedAt()).isNotNull();
        assertThat(loadedNote.toString()).contains("1.0.0");

        ResourceFile loadedFile = resourceFiles.findByToolIdAndLatest(loaded.getId(),
                Integer.valueOf(ResourceFile.LATEST)).orElseThrow();
        loadedFile.setId(loadedFile.getId());
        assertThat(loadedFile.getDisplayName()).isEqualTo("demo.exe");
        assertThat(loadedFile.getObjectKey()).isEqualTo("1/1.0.0/abc.exe");
        assertThat(loadedFile.getExt()).isEqualTo("exe");
        assertThat(loadedFile.getFileSize()).isEqualTo(1024L);
        assertThat(loadedFile.getSha256()).isEqualTo(HEX_64);
        assertThat(loadedFile.getPlatform()).isEqualTo("Windows");
        assertThat(loadedFile.getLatest()).isEqualTo(ResourceFile.LATEST);
        assertThat(loadedFile.getDownloadCount()).isEqualTo(ResourceFile.DEFAULT_DOWNLOAD_COUNT);
        assertThat(loadedFile.getCreatedAt()).isNotNull();
        assertThat(loadedFile.toString()).contains("abc.exe");

        Message loadedMessage = messages.findById(message.getId()).orElseThrow();
        loadedMessage.setId(loadedMessage.getId());
        assertThat(loadedMessage.getNickname()).isEqualTo("匿名");
        assertThat(loadedMessage.getContactEmail()).isEqualTo("secret@example.com");
        assertThat(loadedMessage.getCategory()).isEqualTo(Message.CATEGORY_BUG);
        assertThat(loadedMessage.getContent()).isEqualTo("打不开");
        assertThat(loadedMessage.getOsPlatform()).isEqualTo("Windows 11");
        assertThat(loadedMessage.getToolVersion()).isEqualTo("1.0.0");
        assertThat(loadedMessage.getReproSteps()).isEqualTo("点击运行");
        assertThat(loadedMessage.getStatus()).isEqualTo(Message.STATUS_PENDING);
        assertThat(loadedMessage.getPinned()).isEqualTo(Message.NOT_PINNED);
        assertThat(loadedMessage.getAuditStatus()).isEqualTo(Message.AUDIT_APPROVED);
        assertThat(loadedMessage.getKeywordFlagged()).isEqualTo(Message.NOT_KEYWORD_FLAGGED);
        assertThat(loadedMessage.getAuditedAt()).isNull();
        assertThat(loadedMessage.getIpHash()).isEqualTo(HEX_64);
        assertThat(loadedMessage.getCreatedAt()).isNotNull();
        assertThat(loadedMessage.toString()).doesNotContain("secret@example.com").doesNotContain(HEX_64);

        MessageReply loadedReply = replies.findByMessageIdOrderByCreatedAtAsc(loadedMessage.getId()).get(0);
        loadedReply.setId(loadedReply.getId());
        assertThat(loadedReply.getMessage().getId()).isEqualTo(loadedMessage.getId());
        assertThat(loadedReply.getContent()).isEqualTo("已收到");
        assertThat(loadedReply.getCreatedAt()).isNotNull();
        assertThat(loadedReply.toString()).contains("MessageReply");

        AdminUser loadedAdmin = admins.findByUsername("neco").orElseThrow();
        loadedAdmin.setId(loadedAdmin.getId());
        assertThat(loadedAdmin.getUsername()).isEqualTo("neco");
        assertThat(loadedAdmin.getPasswordHash()).isEqualTo("salted-hash");
        assertThat(loadedAdmin.getLastLoginAt()).isNull();
        assertThat(loadedAdmin.getFailedAttempts()).isEqualTo(AdminUser.DEFAULT_FAILED_ATTEMPTS);
        assertThat(loadedAdmin.getLastFailedAt()).isNull();
        assertThat(loadedAdmin.toString()).doesNotContain("salted-hash");
        loadedAdmin.setLastFailedAt(EntityTimestamps.now());
        admins.saveAndFlush(loadedAdmin);

        SiteSetting loadedSetting = settings.findById(SiteSetting.SITE_NAME).orElseThrow();
        assertThat(loadedSetting.getKey()).isEqualTo(SiteSetting.SITE_NAME);
        assertThat(loadedSetting.getValue()).isNull();
        assertThat(loadedSetting.getUpdatedAt()).isNotNull();
        assertThat(loadedSetting.toString()).contains(SiteSetting.SITE_NAME);
        loadedSetting.setValue("NecoOcean");
        settings.saveAndFlush(loadedSetting);
        entityManager.clear();
        assertThat(settings.findById(SiteSetting.SITE_NAME).orElseThrow().getValue()).isEqualTo("NecoOcean");
    }
}

package com.necoocean.tools.web.admin;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.necoocean.tools.domain.entity.ResourceFile;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.entity.ToolCategory;
import com.necoocean.tools.domain.repository.AdminUserRepository;
import com.necoocean.tools.domain.repository.ResourceFileRepository;
import com.necoocean.tools.domain.repository.ToolCategoryRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import com.necoocean.tools.service.cos.CosObjectStore;
import com.necoocean.tools.service.cos.MemoryCosObjectStore;
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
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * C-28 / C-29 / C-33：两阶段上传与孤儿清理。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@SpringBootTest
@AutoConfigureMockMvc
class CosUploadWebTest {

    private static final String HEX_64 = "abcdef0123456789".repeat(4);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserRepository adminUsers;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ToolCategoryRepository categories;

    @Autowired
    private ToolRepository tools;

    @Autowired
    private ResourceFileRepository resourceFiles;

    @Autowired
    private CosObjectStore cosObjectStore;

    private SessionContext auth;

    private Integer toolId;

    @BeforeEach
    void setUp() throws Exception {
        if (cosObjectStore instanceof MemoryCosObjectStore memory) {
            memory.clear();
        }
        resourceFiles.deleteAll();
        tools.deleteAll();
        categories.deleteAll();
        AdminAuthSupport.insertAdmin(adminUsers, passwordEncoder);
        auth = AdminAuthSupport.login(mockMvc);

        ToolCategory category = new ToolCategory();
        category.setName(ToolCategory.FALLBACK_NAME);
        category.setSortOrder(Integer.valueOf(4));
        categories.saveAndFlush(category);

        Tool tool = new Tool();
        tool.setName("上传工具");
        tool.setSlug("upload-tool");
        tool.setSummary("两阶段直传");
        tool.setCategory(category);
        tool.setPlatforms("Windows");
        tool.setStatus(Integer.valueOf(Tool.STATUS_PUBLISHED));
        toolId = tools.saveAndFlush(tool).getId();
    }

    @Test
    void uploadTicketCompleteAndRejectBadType() throws Exception {
        String ticketPath = "/api/v1/admin/tools/" + toolId + "/files/upload-ticket";
        mockMvc.perform(AdminAuthSupport.withAuth(post(ticketPath), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"display_name":"bad.sh","ext":"sh","file_size":100,\
                                "version":"1.0.0","platform":"Windows"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(42206));

        MvcResult ticket = mockMvc.perform(AdminAuthSupport.withAuth(post(ticketPath), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"display_name":"demo.zip","ext":"zip","file_size":1024,\
                                "version":"1.0.0","platform":"Windows"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.upload_method").value("PUT"))
                .andExpect(jsonPath("$.data.upload_url", containsString("memory.local/upload/")))
                .andExpect(jsonPath("$.data.object_key", containsString(toolId + "/1.0.0/")))
                .andReturn();

        JsonNode data = objectMapper.readTree(ticket.getResponse().getContentAsString()).path("data");
        int fileId = data.path("file_id").asInt();
        String objectKey = data.path("object_key").asText();

        cosObjectStore.putObjectForTest(objectKey, new byte[] {1, 2, 3});
        mockMvc.perform(AdminAuthSupport.withAuth(post("/api/v1/admin/files/" + fileId + "/complete"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sha256\":\"" + HEX_64 + "\",\"file_size\":1024}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sha256").value(HEX_64))
                .andExpect(jsonPath("$.data.object_status").value(ResourceFile.OBJECT_STATUS_READY))
                .andExpect(jsonPath("$.data.is_latest").value(true));

        assertThat(tools.findById(toolId).orElseThrow().getLatestVersion()).isEqualTo("1.0.0");
    }

    @Test
    void completeWithoutObjectMarksAbnormal() throws Exception {
        String ticketPath = "/api/v1/admin/tools/" + toolId + "/files/upload-ticket";
        MvcResult ticket = mockMvc.perform(AdminAuthSupport.withAuth(post(ticketPath), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"display_name":"miss.zip","ext":"zip","file_size":100,\
                                "version":"2.0.0","platform":"通用"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        int fileId = objectMapper.readTree(ticket.getResponse().getContentAsString()).path("data").path("file_id")
                .asInt();

        mockMvc.perform(AdminAuthSupport.withAuth(post("/api/v1/admin/files/" + fileId + "/complete"), auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sha256\":\"" + HEX_64 + "\",\"file_size\":100}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40405));

        assertThat(resourceFiles.findById(Integer.valueOf(fileId)).orElseThrow().getObjectStatus())
                .isEqualTo(ResourceFile.OBJECT_STATUS_ABNORMAL);
    }

    @Test
    void cleanupOrphansRemovesStaleObjectsAndPendingRows() throws Exception {
        MemoryCosObjectStore memory = (MemoryCosObjectStore) cosObjectStore;
        Instant old = Instant.now().minus(25, ChronoUnit.HOURS);
        memory.putObjectWithTime("orphan/old.bin", new byte[] {9}, old);

        ResourceFile pending = new ResourceFile();
        pending.setTool(tools.findById(toolId).orElseThrow());
        pending.setVersion("0.0.1");
        pending.setDisplayName("pending.zip");
        pending.setObjectKey(toolId + "/0.0.1/pending.zip");
        pending.setExt("zip");
        pending.setFileSize(Long.valueOf(10L));
        pending.setSha256(ResourceFile.PENDING_SHA256);
        pending.setLatest(Integer.valueOf(ResourceFile.NOT_LATEST));
        pending.setObjectStatus(Integer.valueOf(ResourceFile.OBJECT_STATUS_PENDING));
        pending.setCreatedAt(com.necoocean.tools.domain.entity.EntityTimestamps.now().minusHours(25));
        resourceFiles.saveAndFlush(pending);
        memory.putObjectWithTime(pending.getObjectKey(), new byte[] {1}, old);

        mockMvc.perform(AdminAuthSupport.withAuth(post("/api/v1/admin/files/cleanup-orphans"), auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deleted_objects").value(1))
                .andExpect(jsonPath("$.data.deleted_pending_rows").value(1));

        assertThat(resourceFiles.findAll()).isEmpty();
        assertThat(memory.objectExists("orphan/old.bin")).isFalse();
        assertThat(memory.objectExists(pending.getObjectKey())).isFalse();
    }
}

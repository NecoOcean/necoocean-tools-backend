package com.necoocean.tools.web.publicapi;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.common.PageResult;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 从 HTTP 入口确认信封、追踪号、蛇形字段和 JSON 内容类型。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiContractWebTest.ValidationProbeController.class)
class ApiContractWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthReturnsJsonEnvelopeAndTraceId() throws Exception {
        mockMvc.perform(get("/api/v1/public/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.status").value("up"))
                .andExpect(header().exists("X-Trace-Id"));
    }

    @Test
    void unknownPathReturnsJsonInsteadOfHtml() throws Exception {
        mockMvc.perform(get("/no-such-page"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(40400))
                .andExpect(jsonPath("$.message").value("请求的资源不存在。"));
    }

    @Test
    void invalidJsonUsesParamInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/public/__probe/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(42200))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void blankBodyUsesParamInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/public/__probe/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(42200))
                .andExpect(jsonPath("$.message").value("提交内容有误，请检查后重试。"));
    }

    @Test
    void dateAndPageUseSnakeCase() throws Exception {
        OffsetDateTime createdAt = OffsetDateTime.of(2026, 9, 21, 9, 57, 52, 0, ZoneOffset.ofHours(8));
        String json = objectMapper.writeValueAsString(new CreatedAtFixture(createdAt));
        org.assertj.core.api.Assertions.assertThat(json).contains("\"created_at\":\"2026-09-21T09:57:52+08:00\"");

        PageResult<String> page = PageResult.of(java.util.List.of("apk"), 1, 20, 21L);
        String pageJson = objectMapper.writeValueAsString(ApiResponse.success(page));
        org.assertj.core.api.Assertions.assertThat(pageJson).contains("\"page_size\":20");
        org.assertj.core.api.Assertions.assertThat(pageJson).contains("\"total_pages\":2");
        org.assertj.core.api.Assertions.assertThat(pageJson).contains("\"list\":[\"apk\"]");
    }

    /**
     * 时间序列化探针。
     */
    private static final class CreatedAtFixture {

        private final OffsetDateTime createdAt;

        private CreatedAtFixture(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
        }

        /**
         * 创建时间。
         *
         * @return 带偏移的时间
         */
        public OffsetDateTime getCreatedAt() {
            return createdAt;
        }
    }

    /**
     * 只在测试里注册，用来触发请求体校验。
     */
    @RestController
    @RequestMapping("/api/v1/public/__probe")
    static class ValidationProbeController {

        /**
         * 校验探针。
         *
         * @param body 请求体
         * @return 空成功信封
         */
        @PostMapping("/validate")
        public ApiResponse<Void> validate(@jakarta.validation.Valid @RequestBody ProbeBody body) {
            return ApiResponse.success(null);
        }
    }

    /**
     * 校验探针的请求体。
     */
    public static class ProbeBody {

        @NotBlank
        private String nickname;

        /**
         * 昵称。
         *
         * @return 昵称
         */
        public String getNickname() {
            return nickname;
        }

        /**
         * 设置昵称。
         *
         * @param nickname 昵称
         */
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}

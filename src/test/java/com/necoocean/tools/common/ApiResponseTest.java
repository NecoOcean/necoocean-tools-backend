package com.necoocean.tools.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 统一信封不携带失败数据，文本形式也不输出 data。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
class ApiResponseTest {

    @Test
    void successUsesZeroCode() {
        ApiResponse<String> response = ApiResponse.success("tool");
        assertThat(response.getCode()).isZero();
        assertThat(response.getMessage()).isEqualTo("ok");
        assertThat(response.getData()).isEqualTo("tool");
        assertThat(response.toString()).doesNotContain("tool");
    }

    @Test
    void failureKeepsStandardCopyAndNullData() {
        ApiResponse<Void> response = ApiResponse.fail(ErrorCode.MESSAGE_DISABLED);
        assertThat(response.getCode()).isEqualTo(ErrorCode.MESSAGE_DISABLED.getCode());
        assertThat(response.getMessage()).isEqualTo("留言功能暂时关闭。");
        assertThat(response.getData()).isNull();
    }

    @Test
    void failureRejectsNullErrorCode() {
        assertThatThrownBy(() -> ApiResponse.fail(null))
                .isInstanceOf(NullPointerException.class);
    }
}

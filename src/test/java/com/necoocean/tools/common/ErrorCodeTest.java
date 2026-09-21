package com.necoocean.tools.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 错误码与附录 C 的对应关系。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
class ErrorCodeTest {

    private static final int BUSINESS_CODE_COUNT = 30;

    @Test
    void businessCodesMatchAppendix() {
        Set<ErrorCode> frameworkCodes = new HashSet<ErrorCode>();
        frameworkCodes.add(ErrorCode.SUCCESS);
        frameworkCodes.add(ErrorCode.ROUTE_NOT_FOUND);
        long businessCount = Arrays.stream(ErrorCode.values())
                .filter(errorCode -> !frameworkCodes.contains(errorCode))
                .count();
        assertThat(businessCount).isEqualTo(BUSINESS_CODE_COUNT);
    }

    @Test
    void codesAreUniqueAndPrefixMatchesHttpStatus() {
        Set<Integer> codes = new HashSet<Integer>();
        for (ErrorCode errorCode : ErrorCode.values()) {
            assertThat(codes.add(errorCode.getCode())).isTrue();
            if (errorCode.getCode() != 0) {
                assertThat(errorCode.getCode() / 100).isEqualTo(errorCode.getHttpStatus());
            }
            assertThat(errorCode.getMessage()).isNotBlank();
        }
    }

    @Test
    void emailMessageStaysOnTheApprovedCopy() {
        assertThat(ErrorCode.EMAIL_INVALID.getMessage()).isEqualTo("邮箱格式不正确，可留空。");
        assertThat(ErrorCode.INTERNAL_ERROR.getMessage()).isEqualTo("服务繁忙，请稍后再试。");
    }
}

package com.necoocean.tools.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 邮箱、IP 和自由文本的脱敏。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
class LogMaskerTest {

    @Test
    void masksEmailLocalPartAndKeepsDomain() {
        assertThat(LogMasker.maskEmail("neco@example.com")).isEqualTo("n***@example.com");
        assertThat(LogMasker.maskEmail("a@example.com")).isEqualTo("***@example.com");
        assertThat(LogMasker.maskEmail("not-an-email")).isEqualTo("***");
        assertThat(LogMasker.maskEmail(" @example.com")).isEqualTo("***");
        assertThat(LogMasker.maskEmail(null)).isEmpty();
        assertThat(LogMasker.maskEmail("  ")).isEmpty();
    }

    @Test
    void masksIpv4AndIpv6() {
        assertThat(LogMasker.maskIp("203.0.113.8")).isEqualTo("203.0.*.*");
        assertThat(LogMasker.maskIp("2001:db8::1")).isEqualTo("2001:*:*");
        assertThat(LogMasker.maskIp("::1")).isEqualTo("***");
        assertThat(LogMasker.maskIp("not-an-ip")).isEqualTo("***");
        assertThat(LogMasker.maskIp(null)).isEmpty();
        assertThat(LogMasker.maskIp("  ")).isEmpty();
    }

    @Test
    void masksEmailsAndAddressesInsideText() {
        String masked = LogMasker.maskText("mail neco@example.com from 203.0.113.8");
        assertThat(masked).contains("n***@example.com");
        assertThat(masked).contains("203.0.*.*");
        assertThat(masked).doesNotContain("neco@example.com");
        assertThat(masked).doesNotContain("203.0.113.8");
        assertThat(LogMasker.maskText(null)).isEmpty();
        assertThat(LogMasker.maskText("plain")).isEqualTo("plain");
    }

    @Test
    void sanitizeRemovesLineBreaks() {
        assertThat(LogMasker.sanitize("a\r\nb")).isEqualTo("a__b");
        assertThat(LogMasker.sanitize(null)).isEmpty();
        assertThat(LogMasker.maskEmail("neco@example.com\nINJECT")).doesNotContain("\n");
    }
}

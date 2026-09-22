package com.necoocean.tools.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * IP 哈希固定 64 位，内网地址不进入限流桶。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
class ClientIpHasherTest {

    @Test
    void hashesIpWithSalt() {
        String hash = ClientIpHasher.hash("203.0.113.10", "salt");

        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(ClientIpHasher.hash("203.0.113.10", "salt")).isEqualTo(hash);
        assertThat(ClientIpHasher.hash("203.0.113.11", "salt")).isNotEqualTo(hash);
    }

    @Test
    void rejectsBlankSalt() {
        assertThatThrownBy(() -> ClientIpHasher.hash("203.0.113.10", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void skipsPrivateAndLoopbackAddresses() {
        assertThat(ClientIpHasher.participatesInRateLimit("203.0.113.10")).isTrue();
        assertThat(ClientIpHasher.participatesInRateLimit("127.0.0.1")).isFalse();
        assertThat(ClientIpHasher.participatesInRateLimit("10.1.2.3")).isFalse();
        assertThat(ClientIpHasher.participatesInRateLimit("192.168.1.8")).isFalse();
        assertThat(ClientIpHasher.participatesInRateLimit("172.16.0.1")).isFalse();
        assertThat(ClientIpHasher.participatesInRateLimit("::1")).isFalse();
        assertThat(ClientIpHasher.participatesInRateLimit("")).isFalse();
        assertThat(ClientIpHasher.participatesInRateLimit(null)).isFalse();
    }
}

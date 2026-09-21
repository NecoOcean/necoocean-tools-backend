package com.necoocean.tools.common;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 转发头只取第一段，取不到时退回连接地址。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
class ClientIpResolverTest {

    @Test
    void usesFirstForwardedHop() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ClientIpResolver.FORWARDED_FOR_HEADER, " 203.0.113.8, 10.0.0.2 ");
        assertThat(ClientIpResolver.resolve(request)).isEqualTo("203.0.113.8");
    }

    @Test
    void blankFirstHopFallsBackToRemoteAddress() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ClientIpResolver.FORWARDED_FOR_HEADER, " , 10.0.0.2");
        request.setRemoteAddr("127.0.0.1");
        assertThat(ClientIpResolver.resolve(request)).isEqualTo("127.0.0.1");
    }

    @Test
    void missingHeaderUsesRemoteAddress() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.10");
        assertThat(ClientIpResolver.resolve(request)).isEqualTo("192.0.2.10");
    }

    @Test
    void nullRequestIsRejected() {
        HttpServletRequest request = null;
        assertThatThrownBy(() -> ClientIpResolver.resolve(request))
                .isInstanceOf(NullPointerException.class);
    }
}

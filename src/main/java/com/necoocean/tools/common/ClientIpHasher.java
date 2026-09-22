package com.necoocean.tools.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;

/**
 * 把客户端 IP 变成可入库的哈希，并判断该地址是否进入限流桶。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public final class ClientIpHasher {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private static final char COLON = ':';

    private static final int IPV4_PARTS = 4;

    private static final int OCTET_MAX = 255;

    private static final int PRIVATE_10 = 10;

    private static final int LOOPBACK = 127;

    private static final int PRIVATE_192 = 192;

    private static final int PRIVATE_168 = 168;

    private static final int LINK_LOCAL_169 = 169;

    private static final int LINK_LOCAL_254 = 254;

    private static final int PRIVATE_172 = 172;

    private static final int PRIVATE_172_LOW = 16;

    private static final int PRIVATE_172_HIGH = 31;

    private static final String IPV6_LOOPBACK = "::1";

    private static final String IPV6_LINK_LOCAL = "fe80:";

    private static final String IPV6_ULA_FC = "fc";

    private static final String IPV6_ULA_FD = "fd";

    private ClientIpHasher() {
    }

    /**
     * 计算 sha256(ip + 盐)，输出 64 位小写十六进制。
     *
     * @param ip   客户端 IP，不能为空
     * @param salt 服务端盐，不能为空
     * @return 64 位哈希
     */
    public static String hash(String ip, String salt) {
        Objects.requireNonNull(ip, "ip");
        if (salt == null || salt.isBlank()) {
            throw new IllegalArgumentException("ip hash salt is blank");
        }
        byte[] digest = sha256(ip + salt);
        StringBuilder builder = new StringBuilder(digest.length * 2);
        for (byte value : digest) {
            int unsigned = value & 0xff;
            builder.append(HEX[unsigned >>> 4]);
            builder.append(HEX[unsigned & 0x0f]);
        }
        return builder.toString();
    }

    /**
     * 公网地址才进入限流。空地址、回环和内网地址不限流，避免反代配错时把全站算成一个人。
     *
     * @param ip 客户端 IP，可以为空
     * @return 应计数时为 true
     */
    public static boolean participatesInRateLimit(String ip) {
        if (ip == null || ip.isBlank() || ip.indexOf(COLON) >= 0) {
            return isPublicIpv6(ip);
        }
        String[] parts = ip.trim().split("\\.");
        if (parts.length != IPV4_PARTS) {
            return false;
        }
        int[] octets = new int[4];
        for (int index = 0; index < parts.length; index++) {
            try {
                octets[index] = Integer.parseInt(parts[index]);
            } catch (NumberFormatException ex) {
                return false;
            }
            if (octets[index] < 0 || octets[index] > OCTET_MAX) {
                return false;
            }
        }
        if (octets[0] == 0 || octets[0] == PRIVATE_10 || octets[0] == LOOPBACK) {
            return false;
        }
        if (octets[0] == PRIVATE_192 && octets[1] == PRIVATE_168) {
            return false;
        }
        if (octets[0] == LINK_LOCAL_169 && octets[1] == LINK_LOCAL_254) {
            return false;
        }
        return !(octets[0] == PRIVATE_172 && octets[1] >= PRIVATE_172_LOW && octets[1] <= PRIVATE_172_HIGH);
    }

    private static boolean isPublicIpv6(String ip) {
        if (ip == null || ip.isBlank() || ip.indexOf(COLON) < 0) {
            return false;
        }
        String lower = ip.trim().toLowerCase(Locale.ROOT);
        if (IPV6_LOOPBACK.equals(lower) || lower.startsWith(IPV6_LINK_LOCAL) || lower.startsWith(IPV6_ULA_FC)
                || lower.startsWith(IPV6_ULA_FD)) {
            return false;
        }
        return true;
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}

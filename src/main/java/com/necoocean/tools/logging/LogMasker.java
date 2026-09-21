package com.necoocean.tools.logging;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志脱敏。邮箱保留首字符和域名，IPv4 保留前两段，无法识别的值不原样写入日志。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
public final class LogMasker {

    private static final Pattern EMAIL_TOKEN = Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    private static final Pattern IPV4_TOKEN = Pattern.compile(
            "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");

    private static final String REDACTED = "***";

    private static final int SINGLE_CHAR_LOCAL_PART = 1;

    private LogMasker() {
    }

    /**
     * 脱敏单个邮箱。不像邮箱的输入返回星号，避免原样落日志。
     *
     * @param email 原始邮箱，可以为 null
     * @return 脱敏后的文本，不返回 null
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 0 || atIndex >= trimmed.length() - 1) {
            return REDACTED;
        }
        String localPart = trimmed.substring(0, atIndex);
        String domain = trimmed.substring(atIndex + 1);
        String masked;
        if (localPart.length() <= SINGLE_CHAR_LOCAL_PART) {
            masked = REDACTED + "@" + domain;
        } else {
            masked = localPart.charAt(0) + REDACTED + "@" + domain;
        }
        return sanitize(masked);
    }

    /**
     * 脱敏单个 IP。IPv4 保留前两段，带冒号的地址只保留首段。
     *
     * @param ip 原始 IP，可以为 null
     * @return 脱敏后的文本，不返回 null
     */
    public static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "";
        }
        String trimmed = ip.trim();
        Matcher ipv4Matcher = IPV4_TOKEN.matcher(trimmed);
        if (ipv4Matcher.matches()) {
            return ipv4Matcher.group(1) + "." + ipv4Matcher.group(2) + ".*.*";
        }
        int colonIndex = trimmed.indexOf(':');
        if (colonIndex > 0) {
            return sanitize(trimmed.substring(0, colonIndex) + ":*:*");
        }
        return REDACTED;
    }

    /**
     * 替换自由文本中的邮箱和 IPv4。
     *
     * @param text 原始文本，可以为 null
     * @return 脱敏后的文本，不返回 null
     */
    public static String maskText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String emailMasked = replace(EMAIL_TOKEN, text, LogMasker::maskEmail);
        return replace(IPV4_TOKEN, emailMasked, LogMasker::maskIp);
    }

    /**
     * 去掉回车和换行，避免日志注入。
     *
     * @param value 原始文本，可以为 null
     * @return 单行文本，不返回 null
     */
    public static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.replace('\r', '_').replace('\n', '_');
    }

    private static String replace(Pattern pattern, String text, Function<String, String> replacer) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String replacement = Matcher.quoteReplacement(replacer.apply(matcher.group()));
            matcher.appendReplacement(builder, replacement);
        }
        matcher.appendTail(builder);
        return builder.toString();
    }
}

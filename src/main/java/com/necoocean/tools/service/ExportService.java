package com.necoocean.tools.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.entity.ExportAuditLog;
import com.necoocean.tools.domain.entity.Message;
import com.necoocean.tools.domain.entity.MessageReply;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.entity.ToolCategory;
import com.necoocean.tools.domain.repository.ExportAuditLogRepository;
import com.necoocean.tools.domain.repository.MessageReplyRepository;
import com.necoocean.tools.domain.repository.MessageRepository;
import com.necoocean.tools.domain.repository.ToolCategoryRepository;
import com.necoocean.tools.domain.repository.ToolRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * C-42 数据导出。默认不含邮箱；显式 include_email=true 才输出，并写审计。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    private static final String TYPE_TOOLS = "tools";

    private static final String TYPE_MESSAGES = "messages";

    private static final String TYPE_REPLIES = "replies";

    private static final String TYPE_ALL = "all";

    private static final String FORMAT_CSV = "csv";

    private static final String FORMAT_JSON = "json";

    private static final String CSV_TYPE = "text/csv; charset=UTF-8";

    private static final String JSON_TYPE = "application/json; charset=UTF-8";

    private static final String ZIP_TYPE = "application/zip";

    private static final String EMAIL_WARNING = "警告：本导出含 contact_email，请按敏感信息处理。";

    private static final String CSV_QUOTE = "\"";

    private static final String CSV_ESCAPED_QUOTE = "\"\"";

    private static final char CSV_COMMA = ',';

    private static final char CSV_LF = '\n';

    private static final char CSV_CR = '\r';

    private static final byte[] UTF8_BOM = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final ToolRepository tools;

    private final ToolCategoryRepository categories;

    private final MessageRepository messages;

    private final MessageReplyRepository replies;

    private final ExportAuditLogRepository auditLogs;

    private final ObjectMapper objectMapper;

    /**
     * @param tools       工具
     * @param categories  分类
     * @param messages    留言
     * @param replies     回复
     * @param auditLogs   审计
     * @param objectMapper JSON
     */
    public ExportService(ToolRepository tools, ToolCategoryRepository categories, MessageRepository messages,
            MessageReplyRepository replies, ExportAuditLogRepository auditLogs, ObjectMapper objectMapper) {
        this.tools = tools;
        this.categories = categories;
        this.messages = messages;
        this.replies = replies;
        this.auditLogs = auditLogs;
        this.objectMapper = objectMapper;
    }

    /**
     * 导出并写审计。
     *
     * @param type         tools / messages / replies / all
     * @param format       csv / json
     * @param includeEmail 是否含邮箱，默认 false
     * @return 文件
     */
    @Transactional(rollbackFor = Exception.class)
    public ExportFile export(String type, String format, Boolean includeEmail) {
        String normalizedType = requireType(type);
        String normalizedFormat = requireFormat(format);
        boolean withEmail = Boolean.TRUE.equals(includeEmail);
        writeAudit(normalizedType, normalizedFormat, withEmail);
        try {
            if (FORMAT_JSON.equals(normalizedFormat)) {
                return exportJson(normalizedType, withEmail);
            }
            return exportCsv(normalizedType, withEmail);
        } catch (IOException exception) {
            logger.error("export serialize failed", exception);
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private void writeAudit(String type, String format, boolean withEmail) {
        ExportAuditLog log = new ExportAuditLog();
        log.setOperator(currentOperator());
        log.setExportType(type);
        log.setExportFormat(format);
        log.setIncludeEmail(Integer.valueOf(withEmail ? ExportAuditLog.EMAIL_INCLUDED : ExportAuditLog.EMAIL_EXCLUDED));
        auditLogs.saveAndFlush(log);
        logger.info("export audited, operator={}, type={}, format={}, includeEmail={}", log.getOperator(), type, format,
                withEmail);
    }

    private ExportFile exportJson(String type, boolean withEmail) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        if (needsEmailWarning(type, withEmail)) {
            root.put("_warning", EMAIL_WARNING);
        }
        if (includesTools(type)) {
            root.set("categories", categoriesJson());
            root.set("tools", toolsJson());
        }
        if (includesMessages(type)) {
            root.set("messages", messagesJson(withEmail));
        }
        if (includesReplies(type)) {
            root.set("replies", repliesJson());
        }
        byte[] body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(root);
        return new ExportFile(body, JSON_TYPE, "export-" + type + ".json");
    }

    private static boolean needsEmailWarning(String type, boolean withEmail) {
        return withEmail && includesMessages(type);
    }

    private static boolean includesTools(String type) {
        return TYPE_TOOLS.equals(type) || TYPE_ALL.equals(type);
    }

    private static boolean includesMessages(String type) {
        return TYPE_MESSAGES.equals(type) || TYPE_ALL.equals(type);
    }

    private static boolean includesReplies(String type) {
        return TYPE_REPLIES.equals(type) || TYPE_ALL.equals(type);
    }

    private ExportFile exportCsv(String type, boolean withEmail) throws IOException {
        if (TYPE_ALL.equals(type)) {
            return exportCsvZip(withEmail);
        }
        String csv;
        if (TYPE_TOOLS.equals(type)) {
            csv = toolsCsv();
        } else if (TYPE_MESSAGES.equals(type)) {
            csv = messagesCsv(withEmail);
        } else {
            csv = repliesCsv();
        }
        return new ExportFile(withBom(csv), CSV_TYPE, "export-" + type + ".csv");
    }

    private ExportFile exportCsvZip(boolean withEmail) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(buffer, StandardCharsets.UTF_8)) {
            putZip(zip, "categories.csv", categoriesCsv());
            putZip(zip, "tools.csv", toolsCsv());
            putZip(zip, "messages.csv", messagesCsv(withEmail));
            putZip(zip, "replies.csv", repliesCsv());
        }
        return new ExportFile(buffer.toByteArray(), ZIP_TYPE, "export-all.zip");
    }

    private ArrayNode categoriesJson() {
        ArrayNode array = objectMapper.createArrayNode();
        for (ToolCategory category : categories.findAllOrdered()) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", category.getId());
            node.put("name", category.getName());
            node.put("sort_order", category.getSortOrder());
            array.add(node);
        }
        return array;
    }

    private ArrayNode toolsJson() {
        ArrayNode array = objectMapper.createArrayNode();
        for (Tool tool : tools.findAll()) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", tool.getId());
            node.put("name", tool.getName());
            node.put("slug", tool.getSlug());
            node.put("summary", tool.getSummary());
            node.put("category_id", tool.getCategory().getId());
            node.put("category_name", tool.getCategory().getName());
            node.put("platforms", tool.getPlatforms());
            node.put("status", tool.getStatus());
            node.put("latest_version", tool.getLatestVersion());
            node.put("repo_url", tool.getRepoUrl());
            node.put("web_url", tool.getWebUrl());
            if (tool.getDevelopedAt() == null) {
                node.putNull("developed_at");
            } else {
                node.put("developed_at", tool.getDevelopedAt().toString());
            }
            node.put("created_at", textTime(tool.getCreatedAt()));
            node.put("updated_at", textTime(tool.getUpdatedAt()));
            array.add(node);
        }
        return array;
    }

    private ArrayNode messagesJson(boolean withEmail) {
        ArrayNode array = objectMapper.createArrayNode();
        for (Message message : messages.findAll()) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", message.getId());
            node.put("tool_id", message.getTool().getId());
            node.put("tool_slug", message.getTool().getSlug());
            node.put("nickname", message.getNickname());
            if (withEmail) {
                node.put("contact_email", message.getContactEmail());
            }
            node.put("category", message.getCategory());
            node.put("content", message.getContent());
            node.put("os_platform", message.getOsPlatform());
            node.put("tool_version", message.getToolVersion());
            node.put("repro_steps", message.getReproSteps());
            node.put("status", message.getStatus());
            node.put("audit_status", message.getAuditStatus());
            node.put("is_pinned", message.getPinned());
            node.put("keyword_flagged", message.getKeywordFlagged());
            node.put("created_at", textTime(message.getCreatedAt()));
            array.add(node);
        }
        return array;
    }

    private ArrayNode repliesJson() {
        ArrayNode array = objectMapper.createArrayNode();
        for (MessageReply reply : replies.findAll()) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", reply.getId());
            node.put("message_id", reply.getMessage().getId());
            node.put("content", reply.getContent());
            node.put("created_at", textTime(reply.getCreatedAt()));
            array.add(node);
        }
        return array;
    }

    private String categoriesCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append(row("id", "name", "sort_order"));
        for (ToolCategory category : categories.findAllOrdered()) {
            csv.append(row(category.getId(), category.getName(), category.getSortOrder()));
        }
        return csv.toString();
    }

    private String toolsCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append(row("id", "name", "slug", "summary", "category_id", "category_name", "platforms", "status",
                "latest_version", "repo_url", "web_url", "developed_at", "created_at", "updated_at"));
        for (Tool tool : tools.findAll()) {
            csv.append(row(tool.getId(), tool.getName(), tool.getSlug(), tool.getSummary(), tool.getCategory().getId(),
                    tool.getCategory().getName(), tool.getPlatforms(), tool.getStatus(), tool.getLatestVersion(),
                    tool.getRepoUrl(), tool.getWebUrl(),
                    tool.getDevelopedAt() == null ? "" : tool.getDevelopedAt().toString(),
                    textTime(tool.getCreatedAt()), textTime(tool.getUpdatedAt())));
        }
        return csv.toString();
    }

    private String messagesCsv(boolean withEmail) {
        StringBuilder csv = new StringBuilder();
        if (withEmail) {
            csv.append(EMAIL_WARNING).append('\n');
            csv.append(row("id", "tool_id", "tool_slug", "nickname", "contact_email", "category", "content",
                    "os_platform", "tool_version", "repro_steps", "status", "audit_status", "is_pinned",
                    "keyword_flagged", "created_at"));
        } else {
            csv.append(row("id", "tool_id", "tool_slug", "nickname", "category", "content", "os_platform",
                    "tool_version", "repro_steps", "status", "audit_status", "is_pinned", "keyword_flagged",
                    "created_at"));
        }
        for (Message message : messages.findAll()) {
            if (withEmail) {
                csv.append(row(message.getId(), message.getTool().getId(), message.getTool().getSlug(),
                        message.getNickname(), message.getContactEmail(), message.getCategory(), message.getContent(),
                        message.getOsPlatform(), message.getToolVersion(), message.getReproSteps(), message.getStatus(),
                        message.getAuditStatus(), message.getPinned(), message.getKeywordFlagged(),
                        textTime(message.getCreatedAt())));
            } else {
                csv.append(row(message.getId(), message.getTool().getId(), message.getTool().getSlug(),
                        message.getNickname(), message.getCategory(), message.getContent(), message.getOsPlatform(),
                        message.getToolVersion(), message.getReproSteps(), message.getStatus(),
                        message.getAuditStatus(), message.getPinned(), message.getKeywordFlagged(),
                        textTime(message.getCreatedAt())));
            }
        }
        return csv.toString();
    }

    private String repliesCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append(row("id", "message_id", "content", "created_at"));
        for (MessageReply reply : replies.findAll()) {
            csv.append(row(reply.getId(), reply.getMessage().getId(), reply.getContent(),
                    textTime(reply.getCreatedAt())));
        }
        return csv.toString();
    }

    private static void putZip(ZipOutputStream zip, String name, String csv) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(withBom(csv));
        zip.closeEntry();
    }

    private static byte[] withBom(String csv) {
        byte[] content = csv.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[UTF8_BOM.length + content.length];
        System.arraycopy(UTF8_BOM, 0, out, 0, UTF8_BOM.length);
        System.arraycopy(content, 0, out, UTF8_BOM.length, content.length);
        return out;
    }

    private static String row(Object... values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(CSV_COMMA);
            }
            line.append(csvCell(values[i]));
        }
        line.append(CSV_LF);
        return line.toString();
    }

    private static String csvCell(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (needsCsvQuote(text)) {
            return CSV_QUOTE + text.replace(CSV_QUOTE, CSV_ESCAPED_QUOTE) + CSV_QUOTE;
        }
        return text;
    }

    private static boolean needsCsvQuote(String text) {
        return text.indexOf(CSV_COMMA) >= 0 || text.indexOf(CSV_QUOTE.charAt(0)) >= 0 || text.indexOf(CSV_LF) >= 0
                || text.indexOf(CSV_CR) >= 0;
    }

    private static String textTime(java.time.LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return EntityTimestamps.toOffset(value).toString();
    }

    private static String requireType(String type) {
        if (!StringUtils.hasText(type)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        if (!TYPE_TOOLS.equals(normalized) && !TYPE_MESSAGES.equals(normalized) && !TYPE_REPLIES.equals(normalized)
                && !TYPE_ALL.equals(normalized)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return normalized;
    }

    private static String requireFormat(String format) {
        if (!StringUtils.hasText(format)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String normalized = format.trim().toLowerCase(Locale.ROOT);
        if (!FORMAT_CSV.equals(normalized) && !FORMAT_JSON.equals(normalized)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return normalized;
    }

    private static String currentOperator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return authentication.getName();
    }
}

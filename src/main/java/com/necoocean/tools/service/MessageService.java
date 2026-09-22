package com.necoocean.tools.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ClientIpHasher;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.entity.Message;
import com.necoocean.tools.domain.entity.SiteSetting;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.repository.MessageRepository;
import com.necoocean.tools.domain.repository.SiteSettingRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import com.necoocean.tools.dto.publicapi.MessageStatusDto;
import com.necoocean.tools.dto.publicapi.MessageSubmitDto;
import com.necoocean.tools.dto.publicapi.MessageSubmitRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 留言提交与状态查询。关键词只标记，不拦截；一期默认先发后审。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class MessageService {

    /** 同一 IP 哈希 60 秒内最多提交条数。 */
    static final int IP_LIMIT = 5;

    /** 同一 IP 哈希对同一工具 60 秒内最多提交条数。 */
    static final int TOOL_LIMIT = 3;

    private static final int WINDOW_SECONDS = 60;

    private static final int NICKNAME_MAX = 50;

    private static final int CONTENT_MAX = 2000;

    private static final int EMAIL_MAX = 254;

    private static final int PLATFORM_MAX = 50;

    private static final int VERSION_MAX = 32;

    private static final int STEPS_MAX = 1000;

    private static final String DEFAULT_NICKNAME = "匿名";

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final ToolRepository tools;

    private final MessageRepository messages;

    private final SiteSettingRepository settings;

    private final String ipHashSalt;

    /**
     * @param tools      工具
     * @param messages   留言
     * @param settings   站点配置
     * @param ipHashSalt IP 哈希盐，不能为空
     */
    public MessageService(ToolRepository tools, MessageRepository messages, SiteSettingRepository settings,
            @Value("${tools.security.ip-hash-salt}") String ipHashSalt) {
        this.tools = tools;
        this.messages = messages;
        this.settings = settings;
        if (!StringUtils.hasText(ipHashSalt)) {
            throw new IllegalArgumentException("ip hash salt is blank");
        }
        this.ipHashSalt = ipHashSalt;
    }

    /**
     * 提交留言。校验失败和限流都不落库。
     *
     * @param slug     专题页标识
     * @param request  请求体，不能为空
     * @param clientIp 客户端 IP，可以为空
     * @return 状态摘要
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageSubmitDto submit(String slug, MessageSubmitRequest request, String clientIp) {
        if (request == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String nickname = nickname(request.getNickname());
        Integer category = category(request.getCategory());
        String content = content(request.getContent());
        String email = email(request.getContactEmail());
        String osPlatform = bugField(category, request.getOsPlatform(), PLATFORM_MAX);
        String toolVersion = bugField(category, request.getToolVersion(), VERSION_MAX);
        String reproSteps = bugField(category, request.getReproSteps(), STEPS_MAX);
        Tool tool = publishedTool(slug);
        requireBoardEnabled();
        String ipHash = ClientIpHasher.hash(clientIp == null ? "" : clientIp, ipHashSalt);
        requireWithinRateLimit(ipHash, tool.getId(), clientIp);
        Message message = new Message();
        message.setTool(tool);
        message.setNickname(nickname);
        message.setCategory(category);
        message.setContent(content);
        message.setContactEmail(email);
        message.setOsPlatform(osPlatform);
        message.setToolVersion(toolVersion);
        message.setReproSteps(reproSteps);
        message.setIpHash(ipHash);
        message.setAuditStatus(auditStatus());
        message.setKeywordFlagged(Integer.valueOf(keywordFlagged(content) ? Message.KEYWORD_FLAGGED
                : Message.NOT_KEYWORD_FLAGGED));
        messages.saveAndFlush(message);
        return toSubmitDto(message);
    }

    /**
     * 按主键查审核状态。不返回正文和邮箱。
     *
     * @param id 留言主键
     * @return 状态
     */
    @Transactional(readOnly = true)
    public MessageStatusDto getStatus(Integer id) {
        Message message = messages.findById(id).orElseThrow(() -> new BizException(ErrorCode.MESSAGE_NOT_FOUND));
        return new MessageStatusDto(message.getId(), message.getAuditStatus(), auditText(message.getAuditStatus()),
                EntityTimestamps.toOffset(message.getCreatedAt()));
    }

    private void requireWithinRateLimit(String ipHash, Integer toolId, String clientIp) {
        if (!ClientIpHasher.participatesInRateLimit(clientIp)) {
            return;
        }
        LocalDateTime since = EntityTimestamps.now().minusSeconds(WINDOW_SECONDS);
        if (messages.countRecentByIpHashAndTool(ipHash, toolId, since) >= TOOL_LIMIT
                || messages.countRecentByIpHash(ipHash, since) >= IP_LIMIT) {
            throw new BizException(ErrorCode.MESSAGE_RATE_LIMITED);
        }
    }

    private Tool publishedTool(String slug) {
        Tool tool = tools.findBySlug(slug).orElseThrow(() -> new BizException(ErrorCode.TOOL_NOT_FOUND));
        if (tool.getStatus() == null || tool.getStatus().intValue() != Tool.STATUS_PUBLISHED) {
            throw new BizException(ErrorCode.TOOL_NOT_FOUND);
        }
        return tool;
    }

    private void requireBoardEnabled() {
        SiteSetting setting = settings.findById(SiteSetting.MESSAGE_BOARD_ENABLED).orElse(null);
        if (setting == null || !SiteSetting.BOARD_ON.equals(setting.getValue())) {
            throw new BizException(ErrorCode.MESSAGE_DISABLED);
        }
    }

    private Integer auditStatus() {
        SiteSetting setting = settings.findById(SiteSetting.MESSAGE_AUDIT_MODE).orElse(null);
        if (setting != null && SiteSetting.AUDIT_MODE_PRE.equals(setting.getValue())) {
            return Integer.valueOf(Message.AUDIT_PENDING);
        }
        return Integer.valueOf(Message.AUDIT_APPROVED);
    }

    private boolean keywordFlagged(String content) {
        SiteSetting setting = settings.findById(SiteSetting.MESSAGE_KEYWORDS).orElse(null);
        if (setting == null || !StringUtils.hasText(setting.getValue())) {
            return false;
        }
        String normalized = content.toLowerCase(Locale.ROOT);
        String[] words = setting.getValue().split("[\\r\\n,，;；]+");
        for (String word : words) {
            String keyword = word.trim().toLowerCase(Locale.ROOT);
            if (!keyword.isEmpty() && normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private MessageSubmitDto toSubmitDto(Message message) {
        return new MessageSubmitDto(message.getId(), message.getAuditStatus(), auditText(message.getAuditStatus()),
                Boolean.valueOf(message.getKeywordFlagged().intValue() == Message.KEYWORD_FLAGGED),
                EntityTimestamps.toOffset(message.getCreatedAt()));
    }

    private static String auditText(Integer auditStatus) {
        if (auditStatus != null && auditStatus.intValue() == Message.AUDIT_PENDING) {
            return "待审核";
        }
        if (auditStatus != null && auditStatus.intValue() == Message.AUDIT_REJECTED) {
            return "未通过";
        }
        return "已通过";
    }

    private static String nickname(String value) {
        if (!StringUtils.hasText(value)) {
            return DEFAULT_NICKNAME;
        }
        String trimmed = value.trim();
        if (trimmed.length() > NICKNAME_MAX) {
            throw new BizException(ErrorCode.NICKNAME_INVALID);
        }
        return trimmed;
    }

    private static Integer category(Integer value) {
        if (value == null || value.intValue() < Message.CATEGORY_EXPERIENCE
                || value.intValue() > Message.CATEGORY_QUESTION) {
            throw new BizException(ErrorCode.MESSAGE_CATEGORY_INVALID);
        }
        return value;
    }

    private static String content(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BizException(ErrorCode.CONTENT_INVALID);
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.length() > CONTENT_MAX) {
            throw new BizException(ErrorCode.CONTENT_INVALID);
        }
        return trimmed;
    }

    private static String email(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > EMAIL_MAX || !EMAIL.matcher(trimmed).matches()) {
            throw new BizException(ErrorCode.EMAIL_INVALID);
        }
        return trimmed;
    }

    private static String bugField(Integer category, String value, int maxLength) {
        if (category.intValue() != Message.CATEGORY_BUG || !StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new BizException(ErrorCode.BUG_FIELD_INVALID);
        }
        return trimmed;
    }
}

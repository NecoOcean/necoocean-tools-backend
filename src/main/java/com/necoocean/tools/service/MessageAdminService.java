package com.necoocean.tools.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.common.PageQuery;
import com.necoocean.tools.common.PageResult;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.entity.Message;
import com.necoocean.tools.domain.entity.MessageReply;
import com.necoocean.tools.domain.repository.MessageReplyRepository;
import com.necoocean.tools.domain.repository.MessageRepository;
import com.necoocean.tools.dto.admin.IdResponse;
import com.necoocean.tools.dto.admin.MessageAdminDetailDto;
import com.necoocean.tools.dto.admin.MessageAdminListDto;
import com.necoocean.tools.dto.admin.MessageAuditBatchRequest;
import com.necoocean.tools.dto.admin.MessageAuditRequest;
import com.necoocean.tools.dto.admin.MessagePinRequest;
import com.necoocean.tools.dto.admin.MessageReplyWriteRequest;
import com.necoocean.tools.dto.admin.MessageStatsDto;
import com.necoocean.tools.dto.admin.MessageStatusRequest;
import com.necoocean.tools.dto.admin.MessageToolStatsDto;
import com.necoocean.tools.dto.publicapi.CategoryDto;
import com.necoocean.tools.dto.publicapi.MessageReplyPublicDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 后台留言管理。列表不返回邮箱，仅详情返回；审核终态重复返回 40908。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class MessageAdminService {

    private static final Logger logger = LoggerFactory.getLogger(MessageAdminService.class);

    private static final int REPLY_MAX = 2000;

    private final MessageRepository messages;

    private final MessageReplyRepository replies;

    /**
     * @param messages 留言
     * @param replies  回复
     */
    public MessageAdminService(MessageRepository messages, MessageReplyRepository replies) {
        this.messages = messages;
        this.replies = replies;
    }

    /**
     * 留言列表。不含 contact_email。
     *
     * @param toolId         工具，可选
     * @param category       留言分类，可选
     * @param status         处理状态，可选
     * @param auditStatus    审核状态，可选
     * @param keywordFlagged 关键词命中，可选
     * @param hasEmail       是否有邮箱，可选
     * @param dateFrom       起始日，可选
     * @param dateTo         截止日，可选
     * @param page           页码
     * @param pageSize       每页条数
     * @return 分页列表
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<MessageAdminListDto> list(Integer toolId, Integer category, Integer status, Integer auditStatus,
            Boolean keywordFlagged, Boolean hasEmail, LocalDate dateFrom, LocalDate dateTo, Integer page,
            Integer pageSize) {
        PageQuery query = PageQuery.of(page, pageSize);
        Integer flagged = null;
        if (keywordFlagged != null) {
            flagged = Integer.valueOf(keywordFlagged.booleanValue() ? Message.KEYWORD_FLAGGED
                    : Message.NOT_KEYWORD_FLAGGED);
        }
        LocalDateTime from = dateFrom == null ? null : dateFrom.atStartOfDay();
        LocalDateTime to = dateTo == null ? null : dateTo.atTime(LocalTime.MAX).withNano(0);
        Page<Message> rows = messages.findAdmin(toolId, category, status, auditStatus, flagged, hasEmail, from, to,
                PageRequest.of(query.getPage() - 1, query.getPageSize()));
        List<MessageAdminListDto> items = new ArrayList<MessageAdminListDto>(rows.getContent().size());
        for (Message message : rows.getContent()) {
            items.add(toListDto(message));
        }
        return PageResult.of(items, query.getPage(), query.getPageSize(), rows.getTotalElements());
    }

    /**
     * 留言详情。唯一返回邮箱。
     *
     * @param id 主键
     * @return 详情
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public MessageAdminDetailDto get(Integer id) {
        return toDetailDto(requireMessage(id));
    }

    /**
     * 回复留言。
     *
     * @param id      留言主键
     * @param request 回复正文
     * @return 新回复主键
     */
    @Transactional(rollbackFor = Exception.class)
    public IdResponse reply(Integer id, MessageReplyWriteRequest request) {
        Message message = requireMessage(id);
        if (request == null || !StringUtils.hasText(request.getContent())) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String content = request.getContent().trim();
        if (content.isEmpty() || content.length() > REPLY_MAX) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        MessageReply reply = new MessageReply();
        reply.setMessage(message);
        reply.setContent(content);
        replies.saveAndFlush(reply);
        logger.info("message reply created, messageId={}, replyId={}", id, reply.getId());
        return new IdResponse(reply.getId());
    }

    /**
     * 删除回复。
     *
     * @param replyId 回复主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteReply(Integer replyId) {
        if (replyId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        MessageReply reply = replies.findById(replyId)
                .orElseThrow(() -> new BizException(ErrorCode.MESSAGE_NOT_FOUND));
        replies.delete(reply);
        logger.info("message reply deleted, id={}", replyId);
    }

    /**
     * 标记处理状态。
     *
     * @param id      留言主键
     * @param request 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void patchStatus(Integer id, MessageStatusRequest request) {
        Message message = requireMessage(id);
        if (request == null || request.getStatus() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        int status = request.getStatus().intValue();
        if (status < Message.STATUS_PENDING || status > Message.STATUS_IGNORED) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        message.setStatus(Integer.valueOf(status));
        messages.saveAndFlush(message);
        logger.info("message status patched, id={}, status={}", id, status);
    }

    /**
     * 置顶或取消置顶。
     *
     * @param id      留言主键
     * @param request 置顶标志
     */
    @Transactional(rollbackFor = Exception.class)
    public void patchPin(Integer id, MessagePinRequest request) {
        Message message = requireMessage(id);
        if (request == null || request.getPinned() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        message.setPinned(Integer.valueOf(request.getPinned().booleanValue() ? Message.PINNED : Message.NOT_PINNED));
        messages.saveAndFlush(message);
        logger.info("message pin patched, id={}, pinned={}", id, request.getPinned());
    }

    /**
     * 删除留言。级联删回复，邮箱随记录删除。
     *
     * @param id 留言主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        Message message = requireMessage(id);
        messages.delete(message);
        logger.info("message deleted, id={}", id);
    }

    /**
     * 反馈统计。
     *
     * @return 各工具统计
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public MessageStatsDto stats() {
        List<Object[]> rows = messages.countStatsByTool(Integer.valueOf(Message.CATEGORY_BUG),
                Integer.valueOf(Message.CATEGORY_SUGGESTION));
        List<MessageToolStatsDto> items = new ArrayList<MessageToolStatsDto>(rows.size());
        for (Object[] row : rows) {
            Integer toolId = (Integer) row[0];
            String toolName = (String) row[1];
            Long total = ((Number) row[2]).longValue();
            Long bugs = ((Number) row[3]).longValue();
            Long suggestions = ((Number) row[4]).longValue();
            items.add(new MessageToolStatsDto(new CategoryDto(toolId, toolName), total, bugs, suggestions));
        }
        return new MessageStatsDto(items);
    }

    /**
     * 单条审核。终态重复返回 40908。
     *
     * @param id      留言主键
     * @param request 审核结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Integer id, MessageAuditRequest request) {
        Message message = requireMessage(id);
        applyAudit(message, requireAuditResult(request == null ? null : request.getAuditStatus()));
        messages.saveAndFlush(message);
        logger.info("message audited, id={}, auditStatus={}", id, message.getAuditStatus());
    }

    /**
     * 批量审核。任一已是终态则整批失败。
     *
     * @param request 批量请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditBatch(MessageAuditBatchRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        Integer auditStatus = requireAuditResult(request.getAuditStatus());
        for (Integer id : request.getIds()) {
            Message message = requireMessage(id);
            applyAudit(message, auditStatus);
            messages.saveAndFlush(message);
        }
        logger.info("messages audited in batch, size={}", request.getIds().size());
    }

    /**
     * 一键通过全部待审。
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditAllPending() {
        int updated = messages.approveAllPending(Integer.valueOf(Message.AUDIT_PENDING),
                Integer.valueOf(Message.AUDIT_APPROVED), EntityTimestamps.now());
        logger.info("all pending messages approved, count={}", updated);
    }

    private Message requireMessage(Integer id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return messages.findById(id).orElseThrow(() -> new BizException(ErrorCode.MESSAGE_NOT_FOUND));
    }

    private static Integer requireAuditResult(Integer auditStatus) {
        if (auditStatus == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (auditStatus.intValue() != Message.AUDIT_APPROVED && auditStatus.intValue() != Message.AUDIT_REJECTED) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return auditStatus;
    }

    private static void applyAudit(Message message, Integer auditStatus) {
        Integer current = message.getAuditStatus();
        if (isTerminalAudit(current)) {
            throw new BizException(ErrorCode.MESSAGE_ALREADY_AUDITED);
        }
        message.setAuditStatus(auditStatus);
        message.setAuditedAt(EntityTimestamps.now());
    }

    private static boolean isTerminalAudit(Integer current) {
        if (current == null) {
            return false;
        }
        int value = current.intValue();
        return value == Message.AUDIT_APPROVED || value == Message.AUDIT_REJECTED;
    }

    private MessageAdminListDto toListDto(Message message) {
        boolean flagged = Integer.valueOf(Message.KEYWORD_FLAGGED).equals(message.getKeywordFlagged());
        boolean pinned = Integer.valueOf(Message.PINNED).equals(message.getPinned());
        boolean hasEmail = StringUtils.hasText(message.getContactEmail());
        return new MessageAdminListDto(message.getId(),
                new CategoryDto(message.getTool().getId(), message.getTool().getName()), message.getNickname(),
                message.getCategory(), categoryText(message.getCategory()), message.getContent(), message.getStatus(),
                statusText(message.getStatus()), message.getAuditStatus(), auditText(message.getAuditStatus()),
                Boolean.valueOf(flagged), Boolean.valueOf(hasEmail), Boolean.valueOf(pinned),
                EntityTimestamps.toOffset(message.getCreatedAt()));
    }

    private MessageAdminDetailDto toDetailDto(Message message) {
        boolean flagged = Integer.valueOf(Message.KEYWORD_FLAGGED).equals(message.getKeywordFlagged());
        boolean pinned = Integer.valueOf(Message.PINNED).equals(message.getPinned());
        return new MessageAdminDetailDto(message.getId(),
                new CategoryDto(message.getTool().getId(), message.getTool().getName()), message.getNickname(),
                message.getContactEmail(), message.getCategory(), categoryText(message.getCategory()),
                message.getContent(), message.getOsPlatform(), message.getToolVersion(), message.getReproSteps(),
                message.getStatus(), statusText(message.getStatus()), message.getAuditStatus(),
                auditText(message.getAuditStatus()), Boolean.valueOf(flagged), Boolean.valueOf(pinned),
                EntityTimestamps.toOffset(message.getCreatedAt()), EntityTimestamps.toOffset(message.getAuditedAt()),
                repliesOf(message.getId()));
    }

    private List<MessageReplyPublicDto> repliesOf(Integer messageId) {
        List<MessageReply> rows = replies.findByMessageIdOrderByCreatedAtAsc(messageId);
        List<MessageReplyPublicDto> items = new ArrayList<MessageReplyPublicDto>(rows.size());
        for (MessageReply reply : rows) {
            items.add(new MessageReplyPublicDto(reply.getId(), reply.getContent(),
                    EntityTimestamps.toOffset(reply.getCreatedAt())));
        }
        return items;
    }

    private static String categoryText(Integer category) {
        if (Integer.valueOf(Message.CATEGORY_EXPERIENCE).equals(category)) {
            return "使用体验";
        }
        if (Integer.valueOf(Message.CATEGORY_SUGGESTION).equals(category)) {
            return "功能建议";
        }
        if (Integer.valueOf(Message.CATEGORY_BUG).equals(category)) {
            return "BUG报错";
        }
        if (Integer.valueOf(Message.CATEGORY_QUESTION).equals(category)) {
            return "问题咨询";
        }
        return "";
    }

    private static String statusText(Integer status) {
        if (Integer.valueOf(Message.STATUS_HANDLED).equals(status)) {
            return "已处理";
        }
        if (Integer.valueOf(Message.STATUS_FOLLOWED).equals(status)) {
            return "已跟进";
        }
        if (Integer.valueOf(Message.STATUS_IGNORED).equals(status)) {
            return "无需处理";
        }
        return "未处理";
    }

    private static String auditText(Integer auditStatus) {
        if (Integer.valueOf(Message.AUDIT_PENDING).equals(auditStatus)) {
            return "待审核";
        }
        if (Integer.valueOf(Message.AUDIT_REJECTED).equals(auditStatus)) {
            return "未通过";
        }
        return "已通过";
    }
}

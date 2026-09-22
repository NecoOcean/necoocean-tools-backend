package com.necoocean.tools.web.admin;

import java.time.LocalDate;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.common.PageResult;
import com.necoocean.tools.dto.admin.IdResponse;
import com.necoocean.tools.dto.admin.MessageAdminDetailDto;
import com.necoocean.tools.dto.admin.MessageAdminListDto;
import com.necoocean.tools.dto.admin.MessageAuditBatchRequest;
import com.necoocean.tools.dto.admin.MessageAuditRequest;
import com.necoocean.tools.dto.admin.MessagePinRequest;
import com.necoocean.tools.dto.admin.MessageReplyWriteRequest;
import com.necoocean.tools.dto.admin.MessageStatsDto;
import com.necoocean.tools.dto.admin.MessageStatusRequest;
import com.necoocean.tools.service.MessageAdminService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台留言管理。C-34 ~ C-41、C-45 ~ C-47。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
@RequestMapping("/api/v1/admin")
public class MessageAdminController {

    private final MessageAdminService messageAdminService;

    /**
     * @param messageAdminService 留言管理
     */
    public MessageAdminController(MessageAdminService messageAdminService) {
        this.messageAdminService = messageAdminService;
    }

    /**
     * 留言列表。不含邮箱。
     *
     * @param toolId         工具，可选
     * @param category       分类，可选
     * @param status         处理状态，可选
     * @param auditStatus    审核状态，可选
     * @param keywordFlagged 关键词命中，可选
     * @param hasEmail       是否有邮箱，可选
     * @param dateFrom       起始日，可选
     * @param dateTo         截止日，可选
     * @param page           页码
     * @param pageSize       每页条数
     * @param response       当前响应
     * @return 分页列表
     */
    @GetMapping("/messages")
    public ApiResponse<PageResult<MessageAdminListDto>> list(
            @RequestParam(value = "tool_id", required = false) Integer toolId,
            @RequestParam(value = "category", required = false) Integer category,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "audit_status", required = false) Integer auditStatus,
            @RequestParam(value = "keyword_flagged", required = false) Boolean keywordFlagged,
            @RequestParam(value = "has_email", required = false) Boolean hasEmail,
            @RequestParam(value = "date_from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,
            @RequestParam(value = "date_to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page_size", required = false) Integer pageSize, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(messageAdminService.list(toolId, category, status, auditStatus, keywordFlagged,
                hasEmail, dateFrom, dateTo, page, pageSize));
    }

    /**
     * 反馈统计。
     *
     * @param response 当前响应
     * @return 统计
     */
    @GetMapping("/messages/stats")
    public ApiResponse<MessageStatsDto> stats(HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(messageAdminService.stats());
    }

    /**
     * 留言详情。唯一返回邮箱。
     *
     * @param id       主键
     * @param response 当前响应
     * @return 详情
     */
    @GetMapping("/messages/{id}")
    public ApiResponse<MessageAdminDetailDto> get(@PathVariable("id") Integer id, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(messageAdminService.get(id));
    }

    /**
     * 回复留言。
     *
     * @param id       留言主键
     * @param request  回复
     * @param response 当前响应
     * @return 新回复主键
     */
    @PostMapping("/messages/{id}/replies")
    public ApiResponse<IdResponse> reply(@PathVariable("id") Integer id, @RequestBody MessageReplyWriteRequest request,
            HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(messageAdminService.reply(id, request));
    }

    /**
     * 删除回复。
     *
     * @param id       回复主键
     * @param response 当前响应
     * @return 空成功信封
     */
    @DeleteMapping("/replies/{id}")
    public ApiResponse<Void> deleteReply(@PathVariable("id") Integer id, HttpServletResponse response) {
        noStore(response);
        messageAdminService.deleteReply(id);
        return ApiResponse.success(null);
    }

    /**
     * 标记处理状态。
     *
     * @param id       留言主键
     * @param request  状态
     * @param response 当前响应
     * @return 空成功信封
     */
    @PatchMapping("/messages/{id}/status")
    public ApiResponse<Void> patchStatus(@PathVariable("id") Integer id, @RequestBody MessageStatusRequest request,
            HttpServletResponse response) {
        noStore(response);
        messageAdminService.patchStatus(id, request);
        return ApiResponse.success(null);
    }

    /**
     * 置顶或取消置顶。
     *
     * @param id       留言主键
     * @param request  置顶
     * @param response 当前响应
     * @return 空成功信封
     */
    @PatchMapping("/messages/{id}/pin")
    public ApiResponse<Void> patchPin(@PathVariable("id") Integer id, @RequestBody MessagePinRequest request,
            HttpServletResponse response) {
        noStore(response);
        messageAdminService.patchPin(id, request);
        return ApiResponse.success(null);
    }

    /**
     * 删除留言。
     *
     * @param id       留言主键
     * @param response 当前响应
     * @return 空成功信封
     */
    @DeleteMapping("/messages/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Integer id, HttpServletResponse response) {
        noStore(response);
        messageAdminService.delete(id);
        return ApiResponse.success(null);
    }

    /**
     * 单条审核。
     *
     * @param id       留言主键
     * @param request  审核结果
     * @param response 当前响应
     * @return 空成功信封
     */
    @PatchMapping("/messages/{id}/audit")
    public ApiResponse<Void> audit(@PathVariable("id") Integer id, @RequestBody MessageAuditRequest request,
            HttpServletResponse response) {
        noStore(response);
        messageAdminService.audit(id, request);
        return ApiResponse.success(null);
    }

    /**
     * 批量审核。
     *
     * @param request  批量请求
     * @param response 当前响应
     * @return 空成功信封
     */
    @PostMapping("/messages/audit-batch")
    public ApiResponse<Void> auditBatch(@RequestBody MessageAuditBatchRequest request, HttpServletResponse response) {
        noStore(response);
        messageAdminService.auditBatch(request);
        return ApiResponse.success(null);
    }

    /**
     * 一键通过全部待审。
     *
     * @param response 当前响应
     * @return 空成功信封
     */
    @PostMapping("/messages/audit-all-pending")
    public ApiResponse<Void> auditAllPending(HttpServletResponse response) {
        noStore(response);
        messageAdminService.auditAllPending();
        return ApiResponse.success(null);
    }

    private static void noStore(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }
}

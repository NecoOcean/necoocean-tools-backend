package com.necoocean.tools.web.publicapi;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.common.ClientIpResolver;
import com.necoocean.tools.dto.publicapi.MessageStatusDto;
import com.necoocean.tools.dto.publicapi.MessageSubmitDto;
import com.necoocean.tools.dto.publicapi.MessageSubmitRequest;
import com.necoocean.tools.service.MessageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 留言提交和状态查询。公开写接口不要求登录，也不校验 CSRF。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
@RequestMapping("/api/v1/public")
public class MessageController {

    private final MessageService messageService;

    /**
     * @param messageService 留言
     */
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 提交留言。
     *
     * @param slug    专题页标识
     * @param request 请求体
     * @param http    当前请求，用于解析客户端 IP
     * @return 状态摘要
     */
    @PostMapping("/tools/{slug}/messages")
    public ApiResponse<MessageSubmitDto> submit(@PathVariable("slug") String slug,
            @RequestBody MessageSubmitRequest request, HttpServletRequest http) {
        return ApiResponse.success(messageService.submit(slug, request, ClientIpResolver.resolve(http)));
    }

    /**
     * 查询留言审核状态。响应禁止缓存。
     *
     * @param id       留言主键
     * @param response 当前响应
     * @return 状态
     */
    @GetMapping("/messages/{id}/status")
    public ApiResponse<MessageStatusDto> status(@PathVariable("id") Integer id, HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return ApiResponse.success(messageService.getStatus(id));
    }
}

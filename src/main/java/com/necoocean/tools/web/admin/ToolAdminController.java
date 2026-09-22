package com.necoocean.tools.web.admin;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.common.PageResult;
import com.necoocean.tools.dto.admin.IdResponse;
import com.necoocean.tools.dto.admin.StorageUsageDto;
import com.necoocean.tools.dto.admin.ToolAdminDto;
import com.necoocean.tools.dto.admin.ToolCreateRequest;
import com.necoocean.tools.dto.admin.ToolStatusRequest;
import com.necoocean.tools.dto.admin.ToolUpdateRequest;
import com.necoocean.tools.dto.admin.UploadTicketDto;
import com.necoocean.tools.dto.admin.UploadTicketRequest;
import com.necoocean.tools.service.FileUploadService;
import com.necoocean.tools.service.ToolAdminService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台工具管理。C-13 ~ C-19、C-28。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
@RequestMapping("/api/v1/admin/tools")
public class ToolAdminController {

    private final ToolAdminService toolAdminService;

    private final FileUploadService fileUploadService;

    /**
     * @param toolAdminService  工具管理
     * @param fileUploadService 上传凭证
     */
    public ToolAdminController(ToolAdminService toolAdminService, FileUploadService fileUploadService) {
        this.toolAdminService = toolAdminService;
        this.fileUploadService = fileUploadService;
    }

    /**
     * 工具列表。
     *
     * @param status     上下架，可选
     * @param categoryId 分类，可选
     * @param keyword    关键字，可选
     * @param page       页码
     * @param pageSize   每页条数
     * @param response   当前响应
     * @return 分页列表
     */
    @GetMapping
    public ApiResponse<PageResult<ToolAdminDto>> list(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "category_id", required = false) Integer categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page_size", required = false) Integer pageSize, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(toolAdminService.list(status, categoryId, keyword, page, pageSize));
    }

    /**
     * 新建工具。
     *
     * @param request  请求体
     * @param response 当前响应
     * @return 新主键
     */
    @PostMapping
    public ApiResponse<IdResponse> create(@RequestBody ToolCreateRequest request, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(toolAdminService.create(request));
    }

    /**
     * 工具详情。
     *
     * @param id       主键
     * @param response 当前响应
     * @return 详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ToolAdminDto> get(@PathVariable("id") Integer id, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(toolAdminService.get(id));
    }

    /**
     * 编辑工具。
     *
     * @param id       主键
     * @param request  请求体
     * @param response 当前响应
     * @return 详情
     */
    @PutMapping("/{id}")
    public ApiResponse<ToolAdminDto> update(@PathVariable("id") Integer id, @RequestBody ToolUpdateRequest request,
            HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(toolAdminService.update(id, request));
    }

    /**
     * 上下架。
     *
     * @param id       主键
     * @param request  状态
     * @param response 当前响应
     * @return 空成功信封
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> patchStatus(@PathVariable("id") Integer id, @RequestBody ToolStatusRequest request,
            HttpServletResponse response) {
        noStore(response);
        toolAdminService.patchStatus(id, request);
        return ApiResponse.success(null);
    }

    /**
     * 删除工具。删库并删对象存储前缀。
     *
     * @param id       主键
     * @param response 当前响应
     * @return 空成功信封
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Integer id, HttpServletResponse response) {
        noStore(response);
        toolAdminService.delete(id);
        return ApiResponse.success(null);
    }

    /**
     * 申请预签名上传凭证。
     *
     * @param id       工具主键
     * @param request  请求体
     * @param response 当前响应
     * @return 上传凭证
     */
    @PostMapping("/{id}/files/upload-ticket")
    public ApiResponse<UploadTicketDto> uploadTicket(@PathVariable("id") Integer id,
            @RequestBody UploadTicketRequest request, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(fileUploadService.createUploadTicket(id, request));
    }

    /**
     * 存储占用。
     *
     * @param id       工具主键
     * @param response 当前响应
     * @return 占用汇总
     */
    @GetMapping("/{id}/storage-usage")
    public ApiResponse<StorageUsageDto> storageUsage(@PathVariable("id") Integer id, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(toolAdminService.storageUsage(id));
    }

    private static void noStore(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }
}

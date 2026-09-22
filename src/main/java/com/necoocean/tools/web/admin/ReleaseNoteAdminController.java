package com.necoocean.tools.web.admin;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.common.PageResult;
import com.necoocean.tools.dto.admin.IdResponse;
import com.necoocean.tools.dto.admin.ReleaseNoteAdminDto;
import com.necoocean.tools.dto.admin.ReleaseNoteWriteRequest;
import com.necoocean.tools.service.ReleaseNoteAdminService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台更新日志。C-24 ~ C-27。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
@RequestMapping("/api/v1/admin")
public class ReleaseNoteAdminController {

    private final ReleaseNoteAdminService releaseNoteAdminService;

    /**
     * @param releaseNoteAdminService 更新日志管理
     */
    public ReleaseNoteAdminController(ReleaseNoteAdminService releaseNoteAdminService) {
        this.releaseNoteAdminService = releaseNoteAdminService;
    }

    /**
     * 某工具的更新日志列表。
     *
     * @param toolId   工具主键
     * @param page     页码
     * @param pageSize 每页条数
     * @param response 当前响应
     * @return 分页列表
     */
    @GetMapping("/tools/{toolId}/release-notes")
    public ApiResponse<PageResult<ReleaseNoteAdminDto>> list(@PathVariable("toolId") Integer toolId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page_size", required = false) Integer pageSize, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(releaseNoteAdminService.list(toolId, page, pageSize));
    }

    /**
     * 新增更新日志。
     *
     * @param toolId   工具主键
     * @param request  请求体
     * @param response 当前响应
     * @return 新主键
     */
    @PostMapping("/tools/{toolId}/release-notes")
    public ApiResponse<IdResponse> create(@PathVariable("toolId") Integer toolId,
            @RequestBody ReleaseNoteWriteRequest request, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(releaseNoteAdminService.create(toolId, request));
    }

    /**
     * 编辑更新日志。
     *
     * @param id       日志主键
     * @param request  请求体
     * @param response 当前响应
     * @return 详情
     */
    @PutMapping("/release-notes/{id}")
    public ApiResponse<ReleaseNoteAdminDto> update(@PathVariable("id") Integer id,
            @RequestBody ReleaseNoteWriteRequest request, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(releaseNoteAdminService.update(id, request));
    }

    /**
     * 删除更新日志。
     *
     * @param id       日志主键
     * @param response 当前响应
     * @return 空成功信封
     */
    @DeleteMapping("/release-notes/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Integer id, HttpServletResponse response) {
        noStore(response);
        releaseNoteAdminService.delete(id);
        return ApiResponse.success(null);
    }

    private static void noStore(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }
}

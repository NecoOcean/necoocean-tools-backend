package com.necoocean.tools.web.admin;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.dto.admin.IsLatestRequest;
import com.necoocean.tools.dto.admin.ResourceFileAdminDto;
import com.necoocean.tools.dto.admin.ResourceFileUpdateRequest;
import com.necoocean.tools.service.ResourceFileAdminService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台资源文件元信息。C-30 ~ C-32。不含 COS 上传与清理。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
@RequestMapping("/api/v1/admin/files")
public class ResourceFileAdminController {

    private final ResourceFileAdminService resourceFileAdminService;

    /**
     * @param resourceFileAdminService 文件管理
     */
    public ResourceFileAdminController(ResourceFileAdminService resourceFileAdminService) {
        this.resourceFileAdminService = resourceFileAdminService;
    }

    /**
     * 编辑文件元信息。
     *
     * @param id       文件主键
     * @param request  请求体
     * @param response 当前响应
     * @return 更新后的元信息
     */
    @PutMapping("/{id}")
    public ApiResponse<ResourceFileAdminDto> update(@PathVariable("id") Integer id,
            @RequestBody ResourceFileUpdateRequest request, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(resourceFileAdminService.update(id, request));
    }

    /**
     * 设为推荐版本。
     *
     * @param id       文件主键
     * @param request  是否推荐
     * @param response 当前响应
     * @return 更新后的元信息
     */
    @PatchMapping("/{id}/is-latest")
    public ApiResponse<ResourceFileAdminDto> patchLatest(@PathVariable("id") Integer id,
            @RequestBody IsLatestRequest request, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(resourceFileAdminService.patchLatest(id, request));
    }

    /**
     * 删除文件库记录。不删对象存储。
     *
     * @param id       文件主键
     * @param response 当前响应
     * @return 空成功信封
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Integer id, HttpServletResponse response) {
        noStore(response);
        resourceFileAdminService.delete(id);
        return ApiResponse.success(null);
    }

    private static void noStore(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }
}

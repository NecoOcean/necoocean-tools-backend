package com.necoocean.tools.web.admin;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.dto.admin.IsLatestRequest;
import com.necoocean.tools.dto.admin.OrphanCleanupResultDto;
import com.necoocean.tools.dto.admin.ResourceFileAdminDto;
import com.necoocean.tools.dto.admin.ResourceFileUpdateRequest;
import com.necoocean.tools.dto.admin.UploadCompleteRequest;
import com.necoocean.tools.service.FileUploadService;
import com.necoocean.tools.service.OrphanCleanupService;
import com.necoocean.tools.service.ResourceFileAdminService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台资源文件。C-29 ~ C-33。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
@RequestMapping("/api/v1/admin/files")
public class ResourceFileAdminController {

    private final ResourceFileAdminService resourceFileAdminService;

    private final FileUploadService fileUploadService;

    private final OrphanCleanupService orphanCleanupService;

    /**
     * @param resourceFileAdminService 文件管理
     * @param fileUploadService        上传登记
     * @param orphanCleanupService     孤儿清理
     */
    public ResourceFileAdminController(ResourceFileAdminService resourceFileAdminService,
            FileUploadService fileUploadService, OrphanCleanupService orphanCleanupService) {
        this.resourceFileAdminService = resourceFileAdminService;
        this.fileUploadService = fileUploadService;
        this.orphanCleanupService = orphanCleanupService;
    }

    /**
     * 直传完成后登记。
     *
     * @param fileId   占位主键
     * @param request  校验值与大小
     * @param response 当前响应
     * @return 登记后的元信息
     */
    @PostMapping("/{fileId}/complete")
    public ApiResponse<ResourceFileAdminDto> complete(@PathVariable("fileId") Integer fileId,
            @RequestBody UploadCompleteRequest request, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(fileUploadService.completeUpload(fileId, request));
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
     * 删除文件：删库 + 删对象。
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

    /**
     * 手动触发孤儿对象清理。
     *
     * @param response 当前响应
     * @return 清理统计
     */
    @PostMapping("/cleanup-orphans")
    public ApiResponse<OrphanCleanupResultDto> cleanupOrphans(HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(orphanCleanupService.cleanup());
    }

    private static void noStore(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }
}

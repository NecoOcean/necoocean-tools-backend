package com.necoocean.tools.web.admin;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.dto.admin.SiteSettingsAdminDto;
import com.necoocean.tools.dto.admin.SiteSettingsUpdateRequest;
import com.necoocean.tools.service.SiteSettingsAdminService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台站点设置。C-43 ~ C-44。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
@RequestMapping("/api/v1/admin/settings")
public class SiteSettingsAdminController {

    private final SiteSettingsAdminService siteSettingsAdminService;

    /**
     * @param siteSettingsAdminService 站点设置
     */
    public SiteSettingsAdminController(SiteSettingsAdminService siteSettingsAdminService) {
        this.siteSettingsAdminService = siteSettingsAdminService;
    }

    /**
     * 读取全量设置。
     *
     * @param response 当前响应
     * @return 设置
     */
    @GetMapping
    public ApiResponse<SiteSettingsAdminDto> get(HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(siteSettingsAdminService.get());
    }

    /**
     * 部分更新设置。
     *
     * @param request  请求体
     * @param response 当前响应
     * @return 更新后的全量设置
     */
    @PutMapping
    public ApiResponse<SiteSettingsAdminDto> update(@RequestBody SiteSettingsUpdateRequest request,
            HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(siteSettingsAdminService.update(request));
    }

    private static void noStore(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }
}

package com.necoocean.tools.web.admin;

import java.util.List;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.dto.admin.CategoryAdminDto;
import com.necoocean.tools.dto.admin.CategoryWriteRequest;
import com.necoocean.tools.dto.admin.IdResponse;
import com.necoocean.tools.dto.publicapi.PublicListDto;
import com.necoocean.tools.service.CategoryAdminService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台分类管理。C-20 ~ C-23。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
@RequestMapping("/api/v1/admin/categories")
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    /**
     * @param categoryAdminService 分类管理
     */
    public CategoryAdminController(CategoryAdminService categoryAdminService) {
        this.categoryAdminService = categoryAdminService;
    }

    /**
     * 分类列表。
     *
     * @param response 当前响应
     * @return 列表
     */
    @GetMapping
    public ApiResponse<PublicListDto<CategoryAdminDto>> list(HttpServletResponse response) {
        noStore(response);
        List<CategoryAdminDto> items = categoryAdminService.list();
        return ApiResponse.success(new PublicListDto<CategoryAdminDto>(items));
    }

    /**
     * 新增分类。
     *
     * @param request  请求体
     * @param response 当前响应
     * @return 新主键
     */
    @PostMapping
    public ApiResponse<IdResponse> create(@RequestBody CategoryWriteRequest request, HttpServletResponse response) {
        noStore(response);
        return ApiResponse.success(categoryAdminService.create(request));
    }

    /**
     * 编辑分类。
     *
     * @param id       主键
     * @param request  请求体
     * @param response 当前响应
     * @return 空成功信封
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Integer id, @RequestBody CategoryWriteRequest request,
            HttpServletResponse response) {
        noStore(response);
        categoryAdminService.update(id, request);
        return ApiResponse.success(null);
    }

    /**
     * 删除分类。
     *
     * @param id       主键
     * @param response 当前响应
     * @return 空成功信封
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Integer id, HttpServletResponse response) {
        noStore(response);
        categoryAdminService.delete(id);
        return ApiResponse.success(null);
    }

    private static void noStore(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }
}

package com.necoocean.tools.web.publicapi;

import com.necoocean.tools.common.ApiResponse;
import com.necoocean.tools.common.PageResult;
import com.necoocean.tools.dto.publicapi.MessagePublicDto;
import com.necoocean.tools.dto.publicapi.PublicListDto;
import com.necoocean.tools.dto.publicapi.ReleaseNotePublicDto;
import com.necoocean.tools.dto.publicapi.ResourceFilePublicDto;
import com.necoocean.tools.dto.publicapi.SiteInfoDto;
import com.necoocean.tools.dto.publicapi.ToolCardDto;
import com.necoocean.tools.dto.publicapi.ToolDetailDto;
import com.necoocean.tools.service.PublicReadService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 访客可读接口。不要求登录，输出走白名单。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
@RequestMapping("/api/v1/public")
public class PublicReadController {

    private final PublicReadService publicReadService;

    /**
     * @param publicReadService 公开读
     */
    public PublicReadController(PublicReadService publicReadService) {
        this.publicReadService = publicReadService;
    }

    /**
     * 首页工具卡片。
     *
     * @param categoryId 分类，可选
     * @param keyword    关键字，可选
     * @param page       页码，可选
     * @param pageSize   每页条数，可选
     * @return 分页卡片
     */
    @GetMapping("/tools")
    public ApiResponse<PageResult<ToolCardDto>> listTools(
            @RequestParam(value = "category_id", required = false) Integer categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page_size", required = false) Integer pageSize) {
        return ApiResponse.success(publicReadService.listTools(categoryId, keyword, page, pageSize));
    }

    /**
     * 专题页头部。
     *
     * @param slug 专题页标识
     * @return 工具详情
     */
    @GetMapping("/tools/{slug}")
    public ApiResponse<ToolDetailDto> getTool(@PathVariable("slug") String slug) {
        return ApiResponse.success(publicReadService.getTool(slug));
    }

    /**
     * 更新日志。
     *
     * @param slug     专题页标识
     * @param page     页码，可选
     * @param pageSize 每页条数，可选
     * @return 分页日志
     */
    @GetMapping("/tools/{slug}/release-notes")
    public ApiResponse<PageResult<ReleaseNotePublicDto>> listReleaseNotes(@PathVariable("slug") String slug,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page_size", required = false) Integer pageSize) {
        return ApiResponse.success(publicReadService.listReleaseNotes(slug, page, pageSize));
    }

    /**
     * 下载区文件清单。
     *
     * @param slug 专题页标识
     * @return 文件列表
     */
    @GetMapping("/tools/{slug}/files")
    public ApiResponse<PublicListDto<ResourceFilePublicDto>> listFiles(@PathVariable("slug") String slug) {
        return ApiResponse.success(publicReadService.listFiles(slug));
    }

    /**
     * 已通过审核的留言。响应禁止缓存。
     *
     * @param slug     专题页标识
     * @param page     页码，可选
     * @param pageSize 每页条数，可选
     * @param response 当前响应
     * @return 分页留言
     */
    @GetMapping("/tools/{slug}/messages")
    public ApiResponse<PageResult<MessagePublicDto>> listMessages(@PathVariable("slug") String slug,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page_size", required = false) Integer pageSize, HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return ApiResponse.success(publicReadService.listMessages(slug, page, pageSize));
    }

    /**
     * 站点公开信息。允许缓存 60 秒。
     *
     * @param response 当前响应
     * @return 站点信息
     */
    @GetMapping("/site-info")
    public ApiResponse<SiteInfoDto> siteInfo(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=60");
        return ApiResponse.success(publicReadService.getSiteInfo());
    }
}

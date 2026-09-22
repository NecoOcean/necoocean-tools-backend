package com.necoocean.tools.web.admin;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.necoocean.tools.service.ExportFile;
import com.necoocean.tools.service.ExportService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台数据导出。C-42。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
@RequestMapping("/api/v1/admin")
public class ExportAdminController {

    private final ExportService exportService;

    /**
     * @param exportService 导出
     */
    public ExportAdminController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * 导出数据。include_email 默认 false，须显式 true 才含邮箱。
     *
     * @param type         tools / messages / replies / all
     * @param format       csv / json
     * @param includeEmail 是否含邮箱
     * @param response     当前响应
     * @throws Exception 写出失败
     */
    @GetMapping("/export")
    public void export(@RequestParam("type") String type, @RequestParam("format") String format,
            @RequestParam(value = "include_email", required = false) Boolean includeEmail,
            HttpServletResponse response) throws Exception {
        ExportFile file = exportService.export(type, format, includeEmail);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setContentType(file.getContentType());
        String encoded = URLEncoder.encode(file.getFilename(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded);
        response.getOutputStream().write(file.getBody());
        response.getOutputStream().flush();
    }
}

package com.necoocean.tools.web.publicapi;

import com.necoocean.tools.service.PublicReadService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 下载入口。校验后 302 至 COS 预签名地址。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@RestController
public class DownloadController {

    private final PublicReadService publicReadService;

    /**
     * @param publicReadService 公开读
     */
    public DownloadController(PublicReadService publicReadService) {
        this.publicReadService = publicReadService;
    }

    /**
     * 触发下载。不存在或未就绪为 40404；对象缺失为 40405；成功为 302。
     *
     * @param fileId   文件主键
     * @param response 当前响应
     */
    @GetMapping("/download/{fileId}")
    public void download(@PathVariable("fileId") Integer fileId, HttpServletResponse response) {
        String location = publicReadService.prepareDownload(fileId);
        response.setStatus(HttpStatus.FOUND.value());
        response.setHeader(HttpHeaders.LOCATION, location);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }
}

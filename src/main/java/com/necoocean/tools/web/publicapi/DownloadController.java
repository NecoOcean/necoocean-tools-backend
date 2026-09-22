package com.necoocean.tools.web.publicapi;

import com.necoocean.tools.service.PublicReadService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 下载入口。对象存储签名尚未接入，当前只校验文件和上架状态。
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
     * 触发下载。文件不存在或工具已下架返回 40404；记录存在但还不能签名时返回 40405。
     *
     * @param fileId 文件主键
     */
    @GetMapping("/download/{fileId}")
    public void download(@PathVariable("fileId") Integer fileId) {
        publicReadService.requireStoredFile(fileId);
    }
}

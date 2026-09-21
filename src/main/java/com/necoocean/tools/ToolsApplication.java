package com.necoocean.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 后端启动入口。本进程只提供 JSON API，不渲染页面。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
@SpringBootApplication
public class ToolsApplication {

    /**
     * 启动应用。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ToolsApplication.class, args);
    }
}

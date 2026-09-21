package com.necoocean.tools.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动完成日志。不打印命令行参数，避免以后把密钥打进日志。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
@Component
public class ApplicationStartupLogger implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupLogger.class);

    /**
     * 记录进程已启动。
     *
     * @param args 启动参数，本方法不读取其中的值
     */
    @Override
    public void run(ApplicationArguments args) {
        logger.info("application started");
    }
}

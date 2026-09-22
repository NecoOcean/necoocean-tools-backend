package com.necoocean.tools.scheduler;

import com.necoocean.tools.service.OrphanCleanupService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每日孤儿对象清理。判定窗口见 tools.cos.orphan-age-hours。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Component
public class OrphanCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(OrphanCleanupJob.class);

    private final OrphanCleanupService orphanCleanupService;

    /**
     * @param orphanCleanupService 清理服务
     */
    public OrphanCleanupJob(OrphanCleanupService orphanCleanupService) {
        this.orphanCleanupService = orphanCleanupService;
    }

    /**
     * 每天 03:30 执行。
     */
    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Shanghai")
    public void runDaily() {
        logger.info("orphan cleanup job start");
        orphanCleanupService.cleanup();
    }
}

package org.jeffrey.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.activity.service.ArticleStatsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 文章统计数据定时任务
 */
@Slf4j
@Component
public class ArticleStatsScheduler {

    private final ArticleStatsService articleStatsService;

    public ArticleStatsScheduler(ArticleStatsService articleStatsService){
        this.articleStatsService = articleStatsService;
        syncDailyStats();
    }

    /**
     * 每天凌晨1点执行统计数据同步
     */
    @TraceLog("每日文章统计数据同步")
//    @Scheduled(cron = "0 0 1 * * ?")
    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncDailyStats() {
        log.info("Starting daily article stats sync job");
        try {
            articleStatsService.syncTodayStats();
            log.info("Daily article stats sync job completed successfully");
        } catch (Exception e) {
            log.error("Error during daily article stats sync job", e);
        }
    }
} 
package org.jeffrey.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.article.service.ArticleViewCountService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for synchronizing article view counts from Redis to MySQL
 * This separates the scheduling concerns from the service implementation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleViewCountSyncScheduler {

    private final ArticleViewCountService articleViewCountService;

    /**
     * Synchronizes article view counts from Redis to the database.
     * Runs every 5 minutes by default.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    @TraceLog("定时同步文章浏览量到数据库")
    public void syncArticleViewCounts() {
        log.info("Starting scheduled article view count synchronization");
        try {
            articleViewCountService.syncViewCountsToDatabase();
            log.info("Completed scheduled article view count synchronization");
        } catch (Exception e) {
            log.error("Error during scheduled article view count synchronization: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Synchronizes article view counts daily during low-traffic periods.
     * This provides a more thorough sync to ensure data consistency.
     */
    @Scheduled(cron = "0 0 2 * * *") // Every day at 2:00 AM
    @TraceLog("每日全量同步文章浏览量")
    public void dailySyncArticleViewCounts() {
        log.info("Starting daily article view count synchronization");
        try {
            articleViewCountService.syncViewCountsToDatabase();
            log.info("Completed daily article view count synchronization");
        } catch (Exception e) {
            log.error("Error during daily article view count synchronization: {}", e.getMessage(), e);
        }
    }
} 
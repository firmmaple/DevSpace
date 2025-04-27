package org.jeffrey.service.activity.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.core.event.ArticleCollectEvent;
import org.jeffrey.core.event.ArticleInteractionEvent;
import org.jeffrey.core.event.ArticleLikeEvent;
import org.jeffrey.core.event.UserActivityEvent;
import org.jeffrey.api.enums.ActivityTypeEnum;
import org.jeffrey.service.activity.service.UserActivityService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户活动事件监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityEventListener {

    private final UserActivityService userActivityService;
    private final ObjectMapper objectMapper;

    /**
     * 处理用户活动事件
     */
    @Async
    @EventListener
    public void handleUserActivityEvent(UserActivityEvent event) {
        try {
            if (event.getExtraData() != null) {
                userActivityService.recordActivity(
                        event.getUserId(),
                        event.getActivityType(),
                        event.getTargetId(),
                        event.getExtraData());
            } else {
                userActivityService.recordActivity(
                        event.getUserId(),
                        event.getActivityType(),
                        event.getTargetId());
            }
            log.debug("Recorded user activity: {}", event.getActivityType());
        } catch (Exception e) {
            log.error("Failed to record user activity", e);
        }
    }

    /**
     * 处理文章点赞事件
     */
    @Async
    @EventListener
    public void handleArticleLikeEvent(ArticleLikeEvent event) {
        try {
            // 只记录点赞事件，取消点赞不记录
            if (event.isAdd()) {
                userActivityService.recordActivity(
                        event.getUserId(),
                        ActivityTypeEnum.LIKE_ARTICLE,
                        event.getArticleId());
                log.debug("Recorded article like activity for article {}", event.getArticleId());
            }
        } catch (Exception e) {
            log.error("Failed to record article like activity", e);
        }
    }

    /**
     * 处理文章收藏事件
     */
    @Async
    @EventListener
    public void handleArticleCollectEvent(ArticleCollectEvent event) {
        try {
            // 只记录收藏事件，取消收藏不记录
            if (event.isAdd()) {
                userActivityService.recordActivity(
                        event.getUserId(),
                        ActivityTypeEnum.COLLECT_ARTICLE,
                        event.getArticleId());
                log.debug("Recorded article collect activity for article {}", event.getArticleId());
            }
        } catch (Exception e) {
            log.error("Failed to record article collect activity", e);
        }
    }
} 
package org.jeffrey.service.activity.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


} 
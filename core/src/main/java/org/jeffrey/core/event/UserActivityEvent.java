package org.jeffrey.core.event;

import lombok.Getter;
import org.jeffrey.api.enums.ActivityTypeEnum;
import org.springframework.context.ApplicationEvent;

/**
 * 用户活动事件
 */
@Getter
public class UserActivityEvent extends ApplicationEvent {
    private final Long userId;
    private final ActivityTypeEnum activityType;
    private final Long targetId;
    private final String extraData;

    public UserActivityEvent(Object source, Long userId, ActivityTypeEnum activityType, Long targetId) {
        super(source);
        this.userId = userId;
        this.activityType = activityType;
        this.targetId = targetId;
        this.extraData = null;
    }

    public UserActivityEvent(Object source, Long userId, ActivityTypeEnum activityType, Long targetId, String extraData) {
        super(source);
        this.userId = userId;
        this.activityType = activityType;
        this.targetId = targetId;
        this.extraData = extraData;
    }
} 
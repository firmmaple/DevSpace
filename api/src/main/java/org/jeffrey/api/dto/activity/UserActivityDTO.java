package org.jeffrey.api.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jeffrey.api.enums.ActivityTypeEnum;

import java.time.LocalDateTime;

/**
 * 用户活动数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDTO {
    private Long id;
    private Long userId;
    private ActivityTypeEnum activityType;
    private Long targetId;
    private String extraData; // JSON格式存储
    private LocalDateTime createdAt;
} 
package org.jeffrey.service.activity.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户活动实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_activity")
public class UserActivityDO {
    private Long id;
    private Long userId;
    private String activityType;
    private Long targetId;
    private String extraData; // JSON格式存储
    private LocalDateTime createdAt;
} 
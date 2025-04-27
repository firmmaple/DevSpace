package org.jeffrey.api.vo.activity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户活动视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    
    private String username;
    private String avatarUrl;
    
    private String activityType;
    private String activityDescription;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;
    
    private String targetTitle; // 目标标题（如文章标题）
    private String content; // 活动内容（如评论内容）
    
    private LocalDateTime createdAt;
    private String timeAgo; // 友好的时间格式 (e.g. "3小时前")
} 
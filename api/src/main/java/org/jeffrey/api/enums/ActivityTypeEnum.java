package org.jeffrey.api.enums;

import lombok.Getter;

/**
 * 用户活动类型枚举
 */
@Getter
public enum ActivityTypeEnum {
    
    CREATE_ARTICLE("CREATE_ARTICLE", "创建文章"),
    EDIT_ARTICLE("EDIT_ARTICLE", "编辑文章"),
    VIEW_ARTICLE("VIEW_ARTICLE", "浏览文章"),
    LIKE_ARTICLE("LIKE_ARTICLE", "点赞文章"),
    COLLECT_ARTICLE("COLLECT_ARTICLE", "收藏文章"),
    COMMENT("COMMENT", "发表评论");
    
    private final String type;
    private final String description;
    
    ActivityTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }
    
    public static ActivityTypeEnum fromType(String type) {
        for (ActivityTypeEnum activityType : ActivityTypeEnum.values()) {
            if (activityType.getType().equals(type)) {
                return activityType;
            }
        }
        throw new IllegalArgumentException("Unknown activity type: " + type);
    }
} 
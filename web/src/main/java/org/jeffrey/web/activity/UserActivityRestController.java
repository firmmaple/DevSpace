package org.jeffrey.web.activity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.jeffrey.api.enums.ActivityTypeEnum;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.activity.UserActivityVO;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.activity.service.UserActivityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户活动控制器
 */
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class UserActivityRestController {

    private final UserActivityService userActivityService;

    /**
     * 获取用户最近活动
     */
    @TraceLog("获取用户最近活动")
    @GetMapping("/user/{userId}")
    public ResVo<IPage<UserActivityVO>> getUserActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        
        IPage<UserActivityVO> activities = userActivityService.getUserRecentActivities(userId, pageNum, pageSize);
        return ResVo.ok(activities);
    }

    /**
     * 获取用户特定类型的活动
     */
    @TraceLog("获取用户特定类型的活动")
    @GetMapping("/user/{userId}/type/{activityType}")
    public ResVo<IPage<UserActivityVO>> getUserActivitiesByType(
            @PathVariable Long userId,
            @PathVariable String activityType,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        
        ActivityTypeEnum type = ActivityTypeEnum.fromType(activityType);
        IPage<UserActivityVO> activities = userActivityService.getUserActivitiesByType(userId, type, pageNum, pageSize);
        return ResVo.ok(activities);
    }

    /**
     * 获取文章相关的所有活动
     */
    @TraceLog("获取文章相关的所有活动")
    @GetMapping("/article/{articleId}")
    public ResVo<IPage<UserActivityVO>> getArticleActivities(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        
        IPage<UserActivityVO> activities = userActivityService.getArticleActivities(articleId, pageNum, pageSize);
        return ResVo.ok(activities);
    }

    /**
     * 获取自己的活动记录
     */
    @TraceLog("获取自己的活动记录")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-activities")
    public ResVo<IPage<UserActivityVO>> getMyActivities(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String activityType) {
        
        // 从安全上下文中获取当前用户ID
        Long currentUserId = getCurrentUserId();
        
        if (activityType != null && !activityType.isEmpty()) {
            ActivityTypeEnum type = ActivityTypeEnum.fromType(activityType);
            return ResVo.ok(userActivityService.getUserActivitiesByType(currentUserId, type, pageNum, pageSize));
        } else {
            return ResVo.ok(userActivityService.getUserRecentActivities(currentUserId, pageNum, pageSize));
        }
    }
    
    /**
     * 获取当前用户ID
     * 实际实现应从安全上下文中获取
     */
    private Long getCurrentUserId() {
        // TODO: 从Spring Security上下文中获取当前用户ID
        return 1L;
    }
} 
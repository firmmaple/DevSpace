package org.jeffrey.service.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.dto.activity.UserActivityDTO;
import org.jeffrey.api.enums.ActivityTypeEnum;
import org.jeffrey.api.vo.activity.UserActivityVO;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.activity.repository.entity.UserActivityDO;
import org.jeffrey.service.activity.repository.mapper.UserActivityMapper;
import org.jeffrey.service.activity.service.UserActivityService;
import org.jeffrey.service.article.repository.mapper.ArticleMapper;
import org.jeffrey.service.article.service.CommentService;
import org.jeffrey.service.user.repository.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户活动服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

    private final UserActivityMapper userActivityMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final CommentService commentService;

    @Override
    @TraceLog("记录用户活动")
    public Long recordActivity(Long userId, ActivityTypeEnum activityType, Long targetId) {
        return recordActivity(userId, activityType, targetId, null);
    }

    @Override
    @TraceLog("记录用户活动(带额外数据)")
    public Long recordActivity(Long userId, ActivityTypeEnum activityType, Long targetId, String extraData) {
        UserActivityDO activity = UserActivityDO.builder()
                .userId(userId)
                .activityType(activityType.getType())
                .targetId(targetId)
                .extraData(extraData)
                .createdAt(LocalDateTime.now())
                .build();
        
        userActivityMapper.insert(activity);
        return activity.getId();
    }

    @Override
    @TraceLog("获取用户最近活动")
    public IPage<UserActivityVO> getUserRecentActivities(Long userId, long pageNum, long pageSize) {
        Page<UserActivityDO> page = new Page<>(pageNum, pageSize);
        IPage<UserActivityDO> activityPage = userActivityMapper.findUserRecentActivities(userId, page);
        
        return convertToVOPage(activityPage);
    }

    @Override
    @TraceLog("根据活动类型获取用户活动")
    public IPage<UserActivityVO> getUserActivitiesByType(Long userId, ActivityTypeEnum activityType, long pageNum, long pageSize) {
        Page<UserActivityDO> page = new Page<>(pageNum, pageSize);
        IPage<UserActivityDO> activityPage = userActivityMapper.findUserActivitiesByType(userId, activityType.getType(), page);
        
        return convertToVOPage(activityPage);
    }

    @Override
    @TraceLog("获取文章相关的所有活动")
    public IPage<UserActivityVO> getArticleActivities(Long articleId, long pageNum, long pageSize) {
        Page<UserActivityDO> page = new Page<>(pageNum, pageSize);
        IPage<UserActivityDO> activityPage = userActivityMapper.findArticleActivities(articleId, page);
        
        return convertToVOPage(activityPage);
    }

    /**
     * 将DO分页对象转换为VO分页对象
     */
    private IPage<UserActivityVO> convertToVOPage(IPage<UserActivityDO> doPage) {
        List<UserActivityVO> voList = convertToVOList(doPage.getRecords());
        
        // 创建一个新的Page对象来保存转换后的VO列表
        Page<UserActivityVO> voPage = new Page<>(doPage.getCurrent(), doPage.getSize(), doPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    /**
     * 将DO列表转换为VO列表
     */
    private List<UserActivityVO> convertToVOList(List<UserActivityDO> activityList) {
        if (activityList == null || activityList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 收集需要查询的用户ID和文章ID
        List<Long> userIds = activityList.stream()
                .map(UserActivityDO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> articleIds = activityList.stream()
                .filter(a -> a.getActivityType().equals(ActivityTypeEnum.CREATE_ARTICLE.getType()) || 
                             a.getActivityType().equals(ActivityTypeEnum.EDIT_ARTICLE.getType()) || 
                             a.getActivityType().equals(ActivityTypeEnum.VIEW_ARTICLE.getType()) || 
                             a.getActivityType().equals(ActivityTypeEnum.LIKE_ARTICLE.getType()) || 
                             a.getActivityType().equals(ActivityTypeEnum.COLLECT_ARTICLE.getType()))
                .map(UserActivityDO::getTargetId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户和文章信息
        Map<Long, String> userNameMap = new ConcurrentHashMap<>();
        Map<Long, String> userAvatarMap = new ConcurrentHashMap<>();
        Map<Long, String> articleTitleMap = new ConcurrentHashMap<>();
        
        // 查询用户信息
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(user -> {
                userNameMap.put(user.getId(), user.getUsername());
                userAvatarMap.put(user.getId(), user.getAvatarUrl());
            });
        }
        
        // 查询文章信息
        if (!articleIds.isEmpty()) {
            articleMapper.selectBatchIds(articleIds).forEach(article -> {
                articleTitleMap.put(article.getId(), article.getTitle());
            });
        }
        
        // 转换为VO
        return activityList.stream().map(activity -> {
            UserActivityVO vo = new UserActivityVO();
            vo.setId(activity.getId());
            vo.setUserId(activity.getUserId());
            vo.setUsername(userNameMap.getOrDefault(activity.getUserId(), "未知用户"));
            vo.setAvatarUrl(userAvatarMap.get(activity.getUserId()));
            
            ActivityTypeEnum type = ActivityTypeEnum.fromType(activity.getActivityType());
            vo.setActivityType(type.getType());
            vo.setActivityDescription(type.getDescription());
            
            vo.setTargetId(activity.getTargetId());
            vo.setCreatedAt(activity.getCreatedAt());
            vo.setTimeAgo(formatTimeAgo(activity.getCreatedAt()));
            
            // 根据活动类型设置目标标题和内容
            switch (type) {
                case CREATE_ARTICLE:
                case EDIT_ARTICLE:
                case VIEW_ARTICLE:
                case LIKE_ARTICLE:
                case COLLECT_ARTICLE:
                    vo.setTargetTitle(articleTitleMap.getOrDefault(activity.getTargetId(), "未知文章"));
                    break;
                case COMMENT:
                    // 对于评论活动，targetId是文章ID，extraData中包含评论内容
                    vo.setTargetTitle(articleTitleMap.getOrDefault(activity.getTargetId(), "未知文章"));
                    if (activity.getExtraData() != null) {
                        vo.setContent(activity.getExtraData());
                    }
                    break;
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 友好的时间格式化
     */
    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);
        
        long seconds = duration.getSeconds();
        
        if (seconds < 60) {
            return "刚刚";
        } else if (seconds < 3600) {
            return seconds / 60 + "分钟前";
        } else if (seconds < 86400) {
            return seconds / 3600 + "小时前";
        } else if (seconds < 2592000) {
            return seconds / 86400 + "天前";
        } else if (seconds < 31536000) {
            return seconds / 2592000 + "个月前";
        } else {
            return seconds / 31536000 + "年前";
        }
    }
} 
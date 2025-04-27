package org.jeffrey.service.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeffrey.api.dto.activity.UserActivityDTO;
import org.jeffrey.api.enums.ActivityTypeEnum;
import org.jeffrey.api.vo.activity.UserActivityVO;

import java.util.List;

/**
 * 用户活动服务接口
 */
public interface UserActivityService {

    /**
     * 记录用户活动
     * @param userId 用户ID
     * @param activityType 活动类型
     * @param targetId 目标ID
     * @return 活动ID
     */
    Long recordActivity(Long userId, ActivityTypeEnum activityType, Long targetId);

    /**
     * 记录用户活动（带额外数据）
     * @param userId 用户ID
     * @param activityType 活动类型
     * @param targetId 目标ID
     * @param extraData 额外数据（JSON格式）
     * @return 活动ID
     */
    Long recordActivity(Long userId, ActivityTypeEnum activityType, Long targetId, String extraData);

    /**
     * 获取用户最近活动
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页活动列表
     */
    IPage<UserActivityVO> getUserRecentActivities(Long userId, long pageNum, long pageSize);

    /**
     * 根据活动类型获取用户活动
     * @param userId 用户ID
     * @param activityType 活动类型
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页活动列表
     */
    IPage<UserActivityVO> getUserActivitiesByType(Long userId, ActivityTypeEnum activityType, long pageNum, long pageSize);

    /**
     * 获取文章相关的所有活动
     * @param articleId 文章ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页活动列表
     */
    IPage<UserActivityVO> getArticleActivities(Long articleId, long pageNum, long pageSize);
} 
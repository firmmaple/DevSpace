package org.jeffrey.service.activity.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeffrey.service.activity.repository.entity.UserActivityDO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface UserActivityMapper extends BaseMapper<UserActivityDO> {
    
    /**
     * 获取用户最近的活动
     * @param userId 用户ID
     * @param page 分页参数
     * @return 活动列表
     */
    IPage<UserActivityDO> findUserRecentActivities(
            @Param("userId") Long userId,
            @Param("page") Page<UserActivityDO> page);
    
    /**
     * 根据活动类型获取用户活动列表
     * @param userId 用户ID
     * @param activityType 活动类型
     * @param page 分页参数
     * @return 活动列表
     */
    IPage<UserActivityDO> findUserActivitiesByType(
            @Param("userId") Long userId,
            @Param("activityType") String activityType,
            @Param("page") Page<UserActivityDO> page);
    
    /**
     * 获取文章相关的所有活动
     * @param articleId 文章ID
     * @param page 分页参数
     * @return 活动列表
     */
    IPage<UserActivityDO> findArticleActivities(
            @Param("articleId") Long articleId,
            @Param("page") Page<UserActivityDO> page);
} 
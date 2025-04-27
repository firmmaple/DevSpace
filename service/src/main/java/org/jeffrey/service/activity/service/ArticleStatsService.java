package org.jeffrey.service.activity.service;

import org.jeffrey.api.vo.activity.ArticleStatsVO;
import org.jeffrey.service.activity.repository.entity.ArticleDailyStatsDO;

import java.time.LocalDate;
import java.util.List;

/**
 * 文章统计服务接口
 */
public interface ArticleStatsService {

    /**
     * 获取文章统计数据
     * @param articleId 文章ID
     * @param days 天数范围
     * @return 文章统计数据
     */
    ArticleStatsVO getArticleStats(Long articleId, Integer days);

    /**
     * 获取过去N天的热门文章
     * @param days 天数范围
     * @param limit 限制数量
     * @return 热门文章ID列表
     */
    List<Long> getTrendingArticles(Integer days, Integer limit);

    /**
     * 同步今日统计数据
     * 此方法将被定时任务调用
     */
    void syncTodayStats();

    /**
     * 按日期范围获取文章统计数据
     * @param articleId 文章ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    List<ArticleDailyStatsDO> getArticleStatsByDateRange(Long articleId, LocalDate startDate, LocalDate endDate);
} 
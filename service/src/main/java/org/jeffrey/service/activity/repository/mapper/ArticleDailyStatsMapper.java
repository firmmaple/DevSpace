package org.jeffrey.service.activity.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeffrey.service.activity.repository.entity.ArticleDailyStatsDO;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ArticleDailyStatsMapper extends BaseMapper<ArticleDailyStatsDO> {
    
    /**
     * 获取文章指定日期范围内的统计数据
     * @param articleId 文章ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    List<ArticleDailyStatsDO> findArticleStatsByDateRange(
            @Param("articleId") Long articleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 批量更新或插入统计数据
     * @param stats 统计数据列表
     * @return 受影响的行数
     */
    int batchUpsert(@Param("stats") List<ArticleDailyStatsDO> stats);
    
    /**
     * 获取最近一周热门文章（按访问量）
     * @param limit 限制数量
     * @return 文章ID列表及其累计访问量
     */
    List<ArticleDailyStatsDO> findTrendingArticlesByViews(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("limit") Integer limit);
    
    /**
     * 获取过去N天的总数据
     * @param articleId 文章ID
     * @param days 天数
     * @return 汇总的统计数据
     */
    ArticleDailyStatsDO getArticleTotalStatsByDays(
            @Param("articleId") Long articleId,
            @Param("days") Integer days);
} 
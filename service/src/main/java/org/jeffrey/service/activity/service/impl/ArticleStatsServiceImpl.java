package org.jeffrey.service.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.vo.activity.ArticleStatsVO;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.activity.repository.entity.ArticleDailyStatsDO;
import org.jeffrey.service.activity.repository.mapper.ArticleDailyStatsMapper;
import org.jeffrey.service.activity.service.ArticleStatsService;
import org.jeffrey.service.article.repository.entity.*;
import org.jeffrey.service.article.repository.mapper.*;
import org.jeffrey.service.article.service.CommentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章统计服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleStatsServiceImpl implements ArticleStatsService {

    private final ArticleDailyStatsMapper articleDailyStatsMapper;
    private final ArticleMapper articleMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleCollectMapper articleCollectMapper;
    private final ArticleViewCountMapper articleViewCountMapper;
    private final CommentMapper commentMapper;

    @Override
    @TraceLog("获取文章统计数据")
    public ArticleStatsVO getArticleStats(Long articleId, Integer days) {
        // 默认获取过去30天的数据
        int statsDays = days != null && days > 0 ? days : 30;

        // 查询文章信息
        ArticleDO article = articleMapper.selectById(articleId);
        if (article == null) {
            return null;
        }

        // 创建返回对象
        ArticleStatsVO statsVO = new ArticleStatsVO();
        statsVO.setArticleId(articleId);
        statsVO.setTitle(article.getTitle());

        // 获取日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(statsDays - 1);

        // 查询日统计数据
        List<ArticleDailyStatsDO> dailyStats = getArticleStatsByDateRange(articleId, startDate, endDate);

        // 计算总计数据
        int totalViews = 0;
        int totalLikes = 0;
        int totalCollects = 0;
        int totalComments = 0;

        for (ArticleDailyStatsDO stat : dailyStats) {
            totalViews += stat.getViewCount();
            totalLikes += stat.getLikeCount();
            totalCollects += stat.getCollectCount();
            totalComments += stat.getCommentCount();
        }

        statsVO.setTotalViewCount(totalViews);
        statsVO.setTotalLikeCount(totalLikes);
        statsVO.setTotalCollectCount(totalCollects);
        statsVO.setTotalCommentCount(totalComments);

        // 生成日期列表和每日统计数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<String> dates = new ArrayList<>();
        Map<String, List<Integer>> dailyStatsMap = new HashMap<>();

        List<Integer> viewsList = new ArrayList<>();
        List<Integer> likesList = new ArrayList<>();
        List<Integer> collectsList = new ArrayList<>();
        List<Integer> commentsList = new ArrayList<>();

        // 创建一个日期到统计数据的映射
        Map<LocalDate, ArticleDailyStatsDO> statsMap = dailyStats.stream()
                .collect(Collectors.toMap(ArticleDailyStatsDO::getStatDate, stat -> stat));

        // 填充每一天的数据
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dateStr = date.format(formatter);
            dates.add(dateStr);

            ArticleDailyStatsDO stat = statsMap.get(date);
            if (stat != null) {
                viewsList.add(stat.getViewCount());
                likesList.add(stat.getLikeCount());
                collectsList.add(stat.getCollectCount());
                commentsList.add(stat.getCommentCount());
            } else {
                // 没有数据的日期填充0
                viewsList.add(0);
                likesList.add(0);
                collectsList.add(0);
                commentsList.add(0);
            }
        }

        dailyStatsMap.put("views", viewsList);
        dailyStatsMap.put("likes", likesList);
        dailyStatsMap.put("collects", collectsList);
        dailyStatsMap.put("comments", commentsList);

        statsVO.setDates(dates);
        statsVO.setDailyStats(dailyStatsMap);

        // 计算周同比变化率
        calculateWeekOverWeek(statsVO, dailyStats);

        // 默认的访问来源数据（将来可以扩展）
        Map<String, Integer> viewSources = new HashMap<>();
        viewSources.put("direct", (int) (totalViews * 0.7)); // 70%是直接访问
        viewSources.put("search", (int) (totalViews * 0.2)); // 20%是搜索引擎
        viewSources.put("referral", (int) (totalViews * 0.1)); // 10%是引荐链接

        statsVO.setViewSources(viewSources);

        return statsVO;
    }

    @Override
    @TraceLog("获取热门文章")
    public List<Long> getTrendingArticles(Integer days, Integer limit) {
        int statsDays = days != null && days > 0 ? days : 7; // 默认过去7天
        int resultLimit = limit != null && limit > 0 ? limit : 10; // 默认10篇

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(statsDays - 1);

        // 查询热门文章
        List<ArticleDailyStatsDO> trendingArticles = articleDailyStatsMapper.findTrendingArticlesByViews(
                startDate, endDate, resultLimit);

        // 提取文章ID
        return trendingArticles.stream()
                .map(ArticleDailyStatsDO::getArticleId)
                .collect(Collectors.toList());
    }

    @Override
    @TraceLog("同步今日统计数据")
    public void syncTodayStats() {
        LocalDate today = LocalDate.now();

        // 查询所有已发布文章
        LambdaQueryWrapper<ArticleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleDO::getStatus, 1); // 已发布状态
        List<ArticleDO> articles = articleMapper.selectList(queryWrapper);

        List<ArticleDailyStatsDO> statsToUpsert = new ArrayList<>();

        for (ArticleDO article : articles) {
            Long articleId = article.getId();

            // 获取当日浏览量
            Long viewCount = 0L;
            try {
                ArticleViewCountDO viewCountDO = articleViewCountMapper.getViewCountByArticleId(articleId);
                if (viewCountDO != null) {
                    viewCount = viewCountDO.getViewCount();
                } else {
                    log.warn("No view count found for article {}", articleId);
                }
            } catch (Exception e) {
                log.warn("Failed to get view count for article {}", articleId, e);
            }

            // 获取当日点赞数
            Integer likeCount = countArticleLikesToday(articleId);

            // 获取当日收藏数
            Integer collectCount = countArticleCollectsToday(articleId);

            // 获取当日评论数
            Integer commentCount = countArticleCommentsToday(articleId);

            // 构建统计数据
            ArticleDailyStatsDO stats = ArticleDailyStatsDO.builder()
                    .articleId(articleId)
                    .statDate(today)
                    .viewCount(viewCount.intValue())
                    .likeCount(likeCount)
                    .collectCount(collectCount)
                    .commentCount(commentCount)
                    .build();

            statsToUpsert.add(stats);
        }

        // 批量更新或插入
        if (!statsToUpsert.isEmpty()) {
            try {
                articleDailyStatsMapper.batchUpsert(statsToUpsert);
                log.info("Synced today's stats for {} articles", statsToUpsert.size());
            } catch (Exception e) {
                log.error("Failed to sync today's stats", e);
            }
        }
    }

    @Override
    @TraceLog("获取文章日期范围内的统计数据")
    public List<ArticleDailyStatsDO> getArticleStatsByDateRange(Long articleId, LocalDate startDate, LocalDate endDate) {
        return articleDailyStatsMapper.findArticleStatsByDateRange(articleId, startDate, endDate);
    }

    /**
     * 计算周同比变化率
     */
    private void calculateWeekOverWeek(ArticleStatsVO statsVO, List<ArticleDailyStatsDO> dailyStats) {
        // 获取最近一周和前一周的数据
        LocalDate today = LocalDate.now();
        LocalDate oneWeekAgo = today.minusDays(7);
        LocalDate twoWeeksAgo = today.minusDays(14);

        int currentWeekViews = 0;
        int previousWeekViews = 0;
        int currentWeekLikes = 0;
        int previousWeekLikes = 0;
        int currentWeekCollects = 0;
        int previousWeekCollects = 0;

        for (ArticleDailyStatsDO stat : dailyStats) {
            LocalDate date = stat.getStatDate();

            if (!date.isBefore(oneWeekAgo) && !date.isAfter(today)) {
                // 当前一周
                currentWeekViews += stat.getViewCount();
                currentWeekLikes += stat.getLikeCount();
                currentWeekCollects += stat.getCollectCount();
            } else if (!date.isBefore(twoWeeksAgo) && date.isBefore(oneWeekAgo)) {
                // 前一周
                previousWeekViews += stat.getViewCount();
                previousWeekLikes += stat.getLikeCount();
                previousWeekCollects += stat.getCollectCount();
            }
        }

        // 计算变化率
        statsVO.setViewCountWeekOverWeek(calculateChange(currentWeekViews, previousWeekViews));
        statsVO.setLikeCountWeekOverWeek(calculateChange(currentWeekLikes, previousWeekLikes));
        statsVO.setCollectCountWeekOverWeek(calculateChange(currentWeekCollects, previousWeekCollects));
    }

    /**
     * 计算变化率
     */
    private Double calculateChange(int current, int previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100;
    }

    /**
     * 统计当日文章点赞数
     */
    private Integer countArticleLikesToday(Long articleId) {
        try {
            // 1. 获取今天的开始时间 (例如: 2023-10-27 00:00:00)
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();

            // 2. 获取明天的开始时间 (例如: 2023-10-28 00:00:00)
            //    这样查询条件就是 createdAt >= todayStart AND createdAt < tomorrowStart
            LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();

            // 如果你的 createdAt 字段是 java.util.Date 类型，你需要转换:
            // Date todayStartDate = Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant());
            // Date tomorrowStartDate = Date.from(tomorrowStart.atZone(ZoneId.systemDefault()).toInstant());

            LambdaQueryWrapper<ArticleLikeDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(ArticleLikeDO::getArticleId, articleId) // 条件1: 文章ID匹配
                    .ge(ArticleLikeDO::getCreatedAt, todayStart) // 条件2: 创建时间 >= 今天零点
                    .lt(ArticleLikeDO::getCreatedAt, tomorrowStart); // 条件3: 创建时间 < 明天零点

            // 使用 selectCount 方法来获取匹配的记录数
            // BaseMapper<T> 中的 selectCount(Wrapper<T> queryWrapper) 返回 Long 类型
            Long count = articleLikeMapper.selectCount(queryWrapper);

            // 方法签名要求返回 Integer，进行转换。注意潜在的溢出（尽管点赞数通常不会超过Integer.MAX_VALUE）。
            // 如果 count 为 null (理论上 selectCount 不会返回 null，而是 0L)，也处理一下。
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.warn("Failed to count likes for article {}", articleId, e);
            return 0;
        }
    }

    /**
     * 统计当日文章收藏数
     */
    private Integer countArticleCollectsToday(Long articleId) {
        try {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();

            // 假设收藏实体为 ArticleCollectDO，包含 articleId 和 createdAt 字段
            LambdaQueryWrapper<ArticleCollectDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(ArticleCollectDO::getArticleId, articleId) // 匹配文章ID
                    .ge(ArticleCollectDO::getCreatedAt, todayStart) // 创建时间 >= 今天零点
                    .lt(ArticleCollectDO::getCreatedAt, tomorrowStart); // 创建时间 < 明天零点

            // 使用 articleCollectMapper 进行查询
            Long count = articleCollectMapper.selectCount(queryWrapper);

            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            // 记录异常时包含 articleId 和 异常信息
            log.warn("Failed to count today's collects for article {}: {}", articleId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 统计当日文章评论数
     */
    private Integer countArticleCommentsToday(Long articleId) {
        try {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();

            // 假设评论实体为 CommentDO，包含 articleId 和 createdAt 字段
            LambdaQueryWrapper<CommentDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    // 确认 CommentDO 中关联文章的字段是 articleId
                    .eq(CommentDO::getArticleId, articleId) // 匹配文章ID
                    // 确认 CommentDO 中表示创建时间的字段是 createdAt
                    .ge(CommentDO::getCreatedAt, todayStart) // 创建时间 >= 今天零点
                    .lt(CommentDO::getCreatedAt, tomorrowStart); // 创建时间 < 明天零点

            // 使用 commentMapper 进行查询 (需要注入 CommentMapper)
            Long count = commentMapper.selectCount(queryWrapper);

            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            // 记录异常时包含 articleId 和 异常信息
            log.warn("Failed to count today's comments for article {}: {}", articleId, e.getMessage(), e);
            return 0;
        }
    }
}
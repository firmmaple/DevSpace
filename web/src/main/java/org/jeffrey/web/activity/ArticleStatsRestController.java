package org.jeffrey.web.activity;

import lombok.RequiredArgsConstructor;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.Status;
import org.jeffrey.api.vo.StatusEnum;
import org.jeffrey.api.vo.activity.ArticleStatsVO;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.activity.service.ArticleStatsService;
import org.jeffrey.service.article.repository.entity.ArticleDO;
import org.jeffrey.service.article.repository.mapper.ArticleMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章统计数据控制器
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class ArticleStatsRestController {

    private final ArticleStatsService articleStatsService;
    private final ArticleMapper articleMapper;

    /**
     * 获取文章统计数据
     */
    @TraceLog("获取文章统计数据")
    @GetMapping("/article/{articleId}")
    public ResVo<ArticleStatsVO> getArticleStats(
            @PathVariable Long articleId,
            @RequestParam(required = false) Integer days) {
        
        // 检查文章是否存在
        ArticleDO article = articleMapper.selectById(articleId);
        if (article == null) {
            return ResVo.fail(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }
        
        // 检查是否是自己的文章或管理员（如果不公开统计数据）
        // TODO: 实现权限检查
        
        ArticleStatsVO stats = articleStatsService.getArticleStats(articleId, days);
        return ResVo.ok(stats);
    }

    /**
     * 获取热门文章
     */
    @TraceLog("获取热门文章")
    @GetMapping("/trending")
    public ResVo<List<Long>> getTrendingArticles(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Integer limit) {
        
        List<Long> trendingArticles = articleStatsService.getTrendingArticles(days, limit);
        return ResVo.ok(trendingArticles);
    }

    /**
     * 手动触发统计数据同步（仅管理员）
     */
    @TraceLog("手动触发统计数据同步")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync")
    public ResVo<String> triggerStatsSync() {
        try {
            articleStatsService.syncTodayStats();
            return ResVo.ok("Statistics sync triggered successfully");
        } catch (Exception e) {
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, e.getMessage());
        }
    }
} 
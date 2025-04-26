package org.jeffrey.service.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.core.cache.RedisClient;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.article.repository.entity.ArticleViewCountDO;
import org.jeffrey.service.article.repository.mapper.ArticleViewCountMapper;
import org.jeffrey.service.article.service.ArticleViewCountService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleViewCountServiceImpl implements ArticleViewCountService {
    private final ArticleViewCountMapper viewCountMapper;
    
    // Redis key for article view counts hash
    private static final String ARTICLE_VIEWS_KEY = "article_views";
    
    @Override
    @TraceLog("增加文章浏览量")
    public Long incrementViewCount(Long articleId) {
        Long currentCount = RedisClient.hIncr(ARTICLE_VIEWS_KEY, articleId.toString(), 1);
        return currentCount;
    }
    
    @Override
    @TraceLog("获取文章浏览量")
    public Long getViewCount(Long articleId) {
        Long viewCount = RedisClient.hGet(ARTICLE_VIEWS_KEY, articleId.toString(), Long.class);
        
        // If not in Redis, try to get from database
        if (viewCount == null) {
            ArticleViewCountDO viewCountDO = viewCountMapper.selectOne(
                    new LambdaQueryWrapper<ArticleViewCountDO>()
                            .eq(ArticleViewCountDO::getArticleId, articleId)
            );
            
            viewCount = viewCountDO != null ? viewCountDO.getViewCount() : 0L;
            
            // Cache in Redis for future requests
            RedisClient.hSet(ARTICLE_VIEWS_KEY, articleId.toString(), viewCount);
        }
        
        return viewCount;
    }
    
    @Override
    @TraceLog("同步文章浏览量到数据库")
    public void syncViewCountsToDatabase() {
        log.info("Starting to sync article view counts to database");
        
        // Get all article view counts from Redis
        Map<String, Long> allViewCounts = RedisClient.hGetAll(ARTICLE_VIEWS_KEY, Long.class);
        
        if (allViewCounts.isEmpty()) {
            log.info("No article view counts to sync");
            return;
        }
        
        int updateCount = 0;
        int insertCount = 0;
        
        // Update each article's view count in the database
        for (Map.Entry<String, Long> entry : allViewCounts.entrySet()) {
            try {
                Long articleId = Long.parseLong(entry.getKey());
                Long viewCount = entry.getValue();
                
                ArticleViewCountDO existingRecord = viewCountMapper.selectOne(
                        new LambdaQueryWrapper<ArticleViewCountDO>()
                                .eq(ArticleViewCountDO::getArticleId, articleId)
                );
                
                if (existingRecord != null) {
                    // Update existing record
                    existingRecord.setViewCount(viewCount);
                    viewCountMapper.updateById(existingRecord);
                    updateCount++;
                } else {
                    // Create new record
                    ArticleViewCountDO newRecord = new ArticleViewCountDO();
                    newRecord.setArticleId(articleId);
                    newRecord.setViewCount(viewCount);
                    viewCountMapper.insert(newRecord);
                    insertCount++;
                }
            } catch (Exception e) {
                log.error("Error syncing view count for article {}: {}", entry.getKey(), e.getMessage(), e);
            }
        }
        
        log.info("Article view count sync completed: {} records updated, {} records inserted", 
                updateCount, insertCount);
    }
} 
package org.jeffrey.service.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage; // For pagination
import org.jeffrey.api.dto.article.ArticleCreateDTO;
import org.jeffrey.api.dto.article.ArticleUpdateDTO;
import org.jeffrey.api.vo.Article.ArticleSummaryVO;
import org.jeffrey.api.vo.Article.ArticleVO;

public interface ArticleService {
    ArticleVO createArticle(ArticleCreateDTO createDTO, Long authorId);

    ArticleVO getArticleById(Long articleId, Long currentUserId); // Pass currentUserId for interaction status later

    ArticleVO updateArticle(Long articleId, ArticleUpdateDTO updateDTO, Long currentUserId);

    void deleteArticle(Long articleId, Long currentUserId);

    IPage<ArticleSummaryVO> listArticles(int pageNum, int pageSize, Long authorId, Integer status); // Add filtering/sorting params
    
    // Article interaction methods
    void likeArticle(Long articleId, Long userId);
    
    void unlikeArticle(Long articleId, Long userId);
    
    void collectArticle(Long articleId, Long userId);
    
    void uncollectArticle(Long articleId, Long userId);
    
    boolean isArticleLikedByUser(Long articleId, Long userId);
    
    boolean isArticleCollectedByUser(Long articleId, Long userId);
    
    Long getArticleLikeCount(Long articleId);
    
    Long getArticleCollectCount(Long articleId);
}
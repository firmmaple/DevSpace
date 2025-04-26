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

    /**
     * 获取特定作者的文章总数
     * @param authorId 作者ID
     * @return 文章数量
     */
    long countArticlesByAuthorId(Long authorId);

    /**
     * 获取特定作者的文章获赞总数
     * @param authorId 作者ID
     * @return 获赞总数
     */
    long countArticleLikesByAuthorId(Long authorId);

    /**
     * 获取特定作者的文章被收藏总数
     * @param authorId 作者ID
     * @return 收藏总数
     */
    long countArticleCollectsByAuthorId(Long authorId);
    
    /**
     * 获取用户收藏的文章列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    IPage<ArticleVO> getUserCollectedArticles(Long userId, int pageNum, int pageSize);
}
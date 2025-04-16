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
    // Add methods for interactions later
}
package org.jeffrey.service.article.service;

import org.jeffrey.api.es.ArticleEsDoc;
import org.jeffrey.service.article.repository.entity.ArticleDO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SearchService {
    /**
     * 搜索文章
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 文章列表
     */
    Page<ArticleEsDoc> search(String keyword, int page, int size);
    
    /**
     * 将单篇文章索引到Elasticsearch
     * @param article 文章实体
     * @return 索引ID
     */
    String indexArticle(ArticleDO article);
    
    /**
     * 批量索引文章
     * @param articles 文章列表
     * @return 成功索引的文章数量
     */
    int batchIndexArticles(List<ArticleDO> articles);
    
    /**
     * 从数据库同步所有已发布文章到Elasticsearch
     * @return 同步的文章数量
     */
    int syncAllArticles();
    
    /**
     * 删除文章索引
     * @param articleId 文章ID
     */
    void deleteArticleIndex(Long articleId);
}

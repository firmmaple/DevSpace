package org.jeffrey.service.article.service;

public interface ArticleViewCountService {
    /**
     * Increment the view count for an article
     * @param articleId The article ID
     * @return The current view count after incrementing
     */
    Long incrementViewCount(Long articleId);
    
    /**
     * Get the view count for an article
     * @param articleId The article ID
     * @return The current view count
     */
    Long getViewCount(Long articleId);
    
    /**
     * Sync view counts from Redis to database
     * This method will be called by a scheduler
     */
    void syncViewCountsToDatabase();
} 
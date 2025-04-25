package org.jeffrey.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeffrey.service.article.repository.entity.ArticleCollectDO;

@Mapper
public interface ArticleCollectMapper extends BaseMapper<ArticleCollectDO> {
    /**
     * Count the collections of an article
     */
    Long countByArticleId(@Param("articleId") Long articleId);
    
    /**
     * Check if a user has collected an article
     */
    boolean existsByArticleIdAndUserId(@Param("articleId") Long articleId, @Param("userId") Long userId);
} 
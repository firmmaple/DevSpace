package org.jeffrey.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeffrey.service.article.repository.entity.ArticleLikeDO;

@Mapper
public interface ArticleLikeMapper extends BaseMapper<ArticleLikeDO> {
    /**
     * Count the likes of an article
     */
    Long countByArticleId(@Param("articleId") Long articleId);
    
    /**
     * Check if a user has liked an article
     */
    boolean existsByArticleIdAndUserId(@Param("articleId") Long articleId, @Param("userId") Long userId);
} 
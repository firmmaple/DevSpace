package org.jeffrey.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeffrey.service.article.repository.entity.CommentDO;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<CommentDO> {
    /**
     * Get top-level comments of an article with pagination
     */
    IPage<CommentDO> findByArticleIdAndParentIdIsNull(
            @Param("page") Page<CommentDO> page,
            @Param("articleId") Long articleId
    );
    
    /**
     * Get child comments of a parent comment
     */
    List<CommentDO> findByParentId(@Param("parentId") Long parentId);

    List<CommentDO> findByArticleId(@Param("articleId") Long articleId);
    
    /**
     * Count the comments of an article
     */
    Long countByArticleId(@Param("articleId") Long articleId);
} 
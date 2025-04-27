package org.jeffrey.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.jeffrey.service.article.repository.entity.ArticleViewCountDO;

@Mapper
public interface ArticleViewCountMapper extends BaseMapper<ArticleViewCountDO> {
    /**
     * 根据文章ID获取浏览量
     *
     * @param articleId 文章ID
     * @return 浏览量
     */
    @Select("SELECT * FROM article_viewcount WHERE article_id = #{articleId}")
    ArticleViewCountDO getViewCountByArticleId(Long articleId);

    /**
     * 更新浏览量
     *
     * @param articleId 文章ID
     * @param viewCount 浏览量
     */
    @Select("UPDATE article_viewcount SET view_count = #{viewCount} WHERE article_id = #{articleId}")
    void updateViewCount(Long articleId, Long viewCount);
} 
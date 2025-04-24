package org.jeffrey.service.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeffrey.api.dto.interaction.CommentDTO;
import org.jeffrey.api.vo.comment.CommentVO;

public interface CommentService {
    /**
     * Add a comment to an article
     */
    CommentVO addComment(CommentDTO commentDTO);
    
    /**
     * Delete a comment
     */
    void deleteComment(Long commentId, Long userId);
    
    /**
     * Get comments for an article with pagination
     */
    IPage<CommentVO> getArticleComments(Long articleId, int pageNum, int pageSize);
    
    /**
     * Get the count of comments for an article
     */
    Long getArticleCommentCount(Long articleId);
} 
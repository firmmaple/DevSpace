package org.jeffrey.api.vo.Article;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ArticleVO implements Serializable { // For detailed view
    private Long id;
    private String title;
    private String summary;
    private String content; // Include content for detail view
    private Long authorId;
    private String authorUsername; // Populate this in the service layer
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Add interaction counts later (views, likes, collects)
    private Long viewCount;
    private Long likeCount;
    private Long collectCount;
    // Add flags for current user's interaction status later
    private Boolean likedByCurrentUser;
    private Boolean collectedByCurrentUser;
}
package org.jeffrey.api.vo.Article;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleSummaryVO { // For list view
    private Long id;
    private String title;
    private String summary;
    // No content in summary view
    private Long authorId;
    private String authorUsername; // Populate this in the service layer
    private Integer status; // Article status: 1-published, 0-draft, 2-deleted
    private LocalDateTime createdAt;
    // Add interaction counts later
    private Long viewCount;
    private Long likeCount;
    private Long collectCount;
}
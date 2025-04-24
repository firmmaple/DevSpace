package org.jeffrey.api.vo.Article;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleSummaryVO { // For list view
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private String summary;
    // No content in summary view
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authorId;
    private String authorUsername; // Populate this in the service layer
    private Integer status; // Article status: 1-published, 0-draft, 2-deleted
    private LocalDateTime createdAt;
    // Add interaction counts later
    @JsonSerialize(using = ToStringSerializer.class)
    private Long viewCount;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long likeCount;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long collectCount;
}
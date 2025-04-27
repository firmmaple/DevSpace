package org.jeffrey.api.vo.Article;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleSummaryVO { // For list view
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private String summary;
    private String imageUrl; // 文章封面图片URL
    // No content in summary view
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authorId;
    private String authorUsername; // Populate this in the service layer
    private Integer status; // Article status: 1-published, 0-draft, 2-deleted
    private Boolean isHot; // 是否为热门文章
    private LocalDateTime createdAt;
    // Add interaction counts later
    @JsonSerialize(using = ToStringSerializer.class)
    private Long viewCount;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long likeCount;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long collectCount;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long commentCount;
    private List<String> tags;
}
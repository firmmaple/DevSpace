package org.jeffrey.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_viewcount")
public class ArticleViewCountDO {
    private Long id;
    private Long articleId;
    private Long viewCount;
    private LocalDateTime updatedAt;
} 
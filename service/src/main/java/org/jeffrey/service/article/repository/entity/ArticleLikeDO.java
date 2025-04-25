package org.jeffrey.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_like")
public class ArticleLikeDO {
    private Long id;
    private Long articleId;
    private Long userId;
    private LocalDateTime createdAt;
} 
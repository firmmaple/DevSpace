package org.jeffrey.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class CommentDO {
    private Long id;
    private Long articleId;
    private Long userId;
    private String content;
    private Long parentId; // For reply comments, null for top-level comments
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 
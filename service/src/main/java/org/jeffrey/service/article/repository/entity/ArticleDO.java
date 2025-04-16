package org.jeffrey.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class ArticleDO {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Long authorId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

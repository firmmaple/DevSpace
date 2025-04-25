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
    // 0: 草稿, 1: 已发布, 2: 已删除
    // TODO: 使用Enum代替
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

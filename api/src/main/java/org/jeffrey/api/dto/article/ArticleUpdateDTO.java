package org.jeffrey.api.dto.article;

import lombok.Data;

@Data
public class ArticleUpdateDTO {
    private String title;
    private String summary;
    private String content;
    private String imageUrl;
    private Integer status; // Optional: Allow changing status
    // ID will be in the path variable usually
}
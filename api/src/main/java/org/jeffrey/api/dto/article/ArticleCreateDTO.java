package org.jeffrey.api.dto.article;

import lombok.Data;

@Data
public class ArticleCreateDTO {
    private String title;
    private String summary;
    private String content;
    private String imageUrl;
    // authorId will likely come from the logged-in user context
}

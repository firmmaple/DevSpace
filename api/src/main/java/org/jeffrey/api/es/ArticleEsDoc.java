package org.jeffrey.api.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch存储的文章文档
 */
@Data
@Document(indexName = "article")
public class ArticleEsDoc {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String summary;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Keyword)
    private String authorId;
    
    @Field(type = FieldType.Keyword)
    private String authorUsername;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Integer)
    private Integer status;
}

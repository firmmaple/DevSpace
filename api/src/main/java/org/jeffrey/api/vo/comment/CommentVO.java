package org.jeffrey.api.vo.comment;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    
    private String username;
    private String avatarUrl; // User's avatar URL
    private String content;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;
    
    private LocalDateTime createdAt;
    private List<CommentVO> replies; // For nested comments
} 
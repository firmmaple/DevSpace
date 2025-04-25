package org.jeffrey.api.dto.interaction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    
    private String content;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId; // For reply comments, null for top-level comments
} 
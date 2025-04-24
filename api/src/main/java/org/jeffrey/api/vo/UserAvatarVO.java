package org.jeffrey.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户头像VO类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAvatarVO implements Serializable {
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "头像URL")
    private String avatarUrl;
} 
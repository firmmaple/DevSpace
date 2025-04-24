package org.jeffrey.api.vo.User;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户视图对象，用于返回给前端
 */
@Data
@NoArgsConstructor
public class UserVO implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private Boolean isAdmin;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

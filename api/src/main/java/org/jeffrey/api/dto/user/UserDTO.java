package org.jeffrey.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private Boolean isAdmin;
    private String avatarUrl;
}

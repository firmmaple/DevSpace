package org.jeffrey.service.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfo {
    private String username;
    private String password;
    private Boolean isAdmin;
}

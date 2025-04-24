package org.jeffrey.api.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateDTO implements Serializable {
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
}

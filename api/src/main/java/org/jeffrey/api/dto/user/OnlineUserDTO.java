package org.jeffrey.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OnlineUserDTO {
    private Long userId;
    private String username;
    private String jwt;
    private LocalDateTime loginTime;
}

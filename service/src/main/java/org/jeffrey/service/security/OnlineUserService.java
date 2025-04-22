package org.jeffrey.service.security;

import lombok.RequiredArgsConstructor;
import org.jeffrey.core.cache.RedisClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OnlineUserService {
//    private final JWTUtil jwtUtil;
    private final long tokenExpire = 60 * 60 * 24 * 2;   // token过期时间, 2 day

    public void save(String username, String token) {
        RedisClient.setStrWithExpire(username, token, tokenExpire);
    }

    public String findByUsername(String username) {
        return RedisClient.getStr(username);
    }

    public void logout(String username) {
        RedisClient.del(username);
    }
}

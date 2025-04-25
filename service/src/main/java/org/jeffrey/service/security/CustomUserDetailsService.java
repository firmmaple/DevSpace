package org.jeffrey.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("开始加载用户信息: {}", username);
        UserDO UserDO = userService.getUserByUsername(username)
                .orElseThrow(() -> {
                    log.debug("用户名不存在！");
                    return new UsernameNotFoundException("用户名不存在！");
                });

        return new CustomUserDetails(UserDO);
    }
}

package org.jeffrey.core.security;

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
    private final UserDoToUserInfoConverter userDoToUserInfoConverter;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDO UserDO = userService.getUserByUsername(username)
                .orElseThrow(() -> {
                    log.debug("用户名不存在！");
                    return new UsernameNotFoundException("用户名不存在！");
                });

        return new CustomUserDetails(
                userDoToUserInfoConverter.convert(UserDO)
        );
    }
}

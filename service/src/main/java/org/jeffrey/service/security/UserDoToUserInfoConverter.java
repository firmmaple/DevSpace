package org.jeffrey.service.security;

import org.jeffrey.service.user.repository.entity.UserDO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserDoToUserInfoConverter implements Converter<UserDO, UserInfo> {
    @Override
    public UserInfo convert(UserDO userDO) {
        return new UserInfo(userDO.getUsername(), userDO.getPassword(), userDO.getIsAdmin());
    }
}

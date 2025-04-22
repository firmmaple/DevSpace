package org.jeffrey.service.security;

import org.jeffrey.api.dto.user.UserDTO;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserDoToUserInfoConverter implements Converter<UserDO, UserDTO> {
    @Override
    public UserDTO convert(UserDO userDO) {
        return new UserDTO(userDO.getId(), userDO.getUsername(), userDO.getPassword(), userDO.getIsAdmin());
    }
}

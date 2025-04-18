package org.jeffrey.service.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.repository.mapper.UserMapper;
import org.jeffrey.service.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserDO> getAllUsers() {
        return this.list();
    }

    @Override
    public Optional<UserDO> getUserByUsername(String username) {
        return baseMapper.findByUsername(username).stream().findFirst();
    }

    @Override
    public boolean registerUser(String username, String password) {
        // Check if username already exists
        if (getUserByUsername(username).isPresent()) {
            return false;
        }

        // Create new user
        UserDO user = new UserDO();
        user.setUsername(username);
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(password));
        user.setIsAdmin(false);

        // Save user to database
        return save(user);
    }
}

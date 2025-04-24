package org.jeffrey.service.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeffrey.api.dto.user.UserUpdateDTO;
import org.jeffrey.api.vo.User.UserVO;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.repository.mapper.UserMapper;
import org.jeffrey.service.user.service.UserService;
import org.jeffrey.service.security.CustomUserDetails;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

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

    /**
     * 更新当前登录用户的资料
     */
    @Override
    public UserVO updateUserProfile(UserUpdateDTO dto) {
        Long currentUserId = getCurrentUserId(); // 获取当前用户ID
        UserDO user = this.getById(currentUserId);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新字段（可扩展为字段校验）
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setAvatarUrl(dto.getAvatarUrl());
        // 处理bio字段，需要在UserDO中添加此字段
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }

        this.updateById(user);

        // 返回展示对象
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 从Spring Security上下文中获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUserId();
        }
        // 如果未获取到认证信息，抛出异常
        throw new RuntimeException("用户未登录");
    }
    
    /**
     * 更新用户头像
     * @param avatar 头像文件
     * @return 头像URL
     */
    @Override
    public String updateUserAvatar(MultipartFile avatar) {
        if (avatar.isEmpty()) {
            throw new RuntimeException("上传的头像文件为空");
        }
        
        // 获取当前登录用户
        Long currentUserId = getCurrentUserId();
        UserDO user = this.getById(currentUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        try {
            // 生成唯一文件名
            String originalFilename = avatar.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // 确保上传目录存在
            String uploadDir = "uploads/avatars";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(avatar.getInputStream(), filePath);
            
            // 更新用户头像URL
            String avatarUrl = "/uploads/avatars/" + filename;
            user.setAvatarUrl(avatarUrl);
            this.updateById(user);
            
            return avatarUrl;
        } catch (IOException e) {
            throw new RuntimeException("保存头像失败: " + e.getMessage());
        }
    }
}

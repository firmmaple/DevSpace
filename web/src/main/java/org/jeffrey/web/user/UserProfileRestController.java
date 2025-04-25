package org.jeffrey.web.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.dto.user.UserUpdateDTO;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.Status;
import org.jeffrey.api.vo.StatusEnum;
import org.jeffrey.api.vo.User.UserVO;
import org.jeffrey.core.security.JWTUtil;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.security.CustomUserDetails;
import org.jeffrey.service.security.OnlineUserService;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户个人资料 REST API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileRestController {

    // 依赖注入
    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final OnlineUserService onlineUserService;
    
    // JWT Cookie相关常量
    private static final String JWT_COOKIE_NAME = "jwt_token";
    private static final int COOKIE_MAX_AGE = 86400; // 1天

    /**
     * 更新用户资料接口
     */
    @PostMapping("/profile")
    @TraceLog("更新用户资料")
    public ResVo<UserVO> updateProfile(@RequestBody UserUpdateDTO dto, HttpServletResponse response) {
        // 获取当前认证用户名
        String currentUsername = getCurrentUsername();
        
        // 更新用户资料
        UserVO updatedUser = userService.updateUserProfile(dto);
        
        // 如果用户名已更改，需要更新JWT令牌
        if (!updatedUser.getUsername().equals(currentUsername)) {
            try {
                // 从数据库中获取更新后的用户信息
                UserDO userDO = userService.getUserByUsername(updatedUser.getUsername())
                        .orElseThrow(() -> new RuntimeException("无法获取更新后的用户信息"));
                
                // 创建新的用户详情对象
                CustomUserDetails userDetails = new CustomUserDetails(userDO);
                
                // 生成新令牌
                String newToken = jwtUtil.generateToken(userDetails);
                
                // 更新cookie
                Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, newToken);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(COOKIE_MAX_AGE);
                // 在生产环境可启用Secure属性
                // jwtCookie.setSecure(true);
                response.addCookie(jwtCookie);
                
                // 更新Redis中的令牌信息
                onlineUserService.save(updatedUser.getUsername(), newToken);
                
                log.info("用户名已更改，JWT令牌已更新: {} -> {}", currentUsername, updatedUser.getUsername());
            } catch (Exception e) {
                log.error("更新JWT令牌失败", e);
                // 记录错误但不影响用户资料更新结果
                // 如果令牌更新失败，用户可能需要重新登录
                return ResVo.ok(updatedUser); // 依然返回更新成功
            }
        }
        
        return ResVo.ok(updatedUser);
    }

    /**
     * 更新用户头像接口
     */
    @PostMapping("/avatar")
    @TraceLog("更新用户头像")
    public ResVo<String> updateAvatar(@RequestParam("avatar") MultipartFile avatar) {
        if (avatar.isEmpty()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "上传的头像文件为空");
        }
        
        try {
            String avatarUrl = userService.updateUserAvatar(avatar);
            return ResVo.ok(avatarUrl);
        } catch (Exception e) {
            log.error("上传头像失败", e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "上传头像失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户资料接口
     */
    @GetMapping("/profile")
    @TraceLog("获取用户资料")
    public ResVo<UserVO> getUserProfile() {
        try {
            Long userId = getCurrentUserId();
            UserDO user = userService.getById(userId);
            if (user == null) {
                return ResVo.fail(StatusEnum.USER_NOT_EXISTS);
            }
            
            UserVO vo = new UserVO();
            org.springframework.beans.BeanUtils.copyProperties(user, vo);
            return ResVo.ok(vo);
        } catch (Exception e) {
            log.error("获取用户资料失败", e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "获取用户资料失败: " + e.getMessage());
        }
    }
    
    /**
     * 从安全上下文中获取当前用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new RuntimeException("用户未登录");
    }
    
    /**
     * 从安全上下文中获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUserId();
        }
        throw new RuntimeException("用户未登录");
    }
} 
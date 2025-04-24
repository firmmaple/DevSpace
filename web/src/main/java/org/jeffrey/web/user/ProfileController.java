package org.jeffrey.web.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jeffrey.api.dto.user.UserUpdateDTO;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.User.UserVO;
import org.jeffrey.core.security.JWTUtil;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.security.CustomUserDetails;
import org.jeffrey.service.security.CustomUserDetailsService;
import org.jeffrey.service.security.OnlineUserService;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    
    // JWT Cookie相关常量
    private static final String JWT_COOKIE_NAME = "jwt_token";
    private static final int COOKIE_MAX_AGE = 86400; // 1天

    /**
     * Renders the user profile page.
     *
     * @param model The Spring UI Model.
     * @return The name of the profile view template.
     */
    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("title", "DevSpace - Profile");
        model.addAttribute("currentPage", "profile");
        model.addAttribute("viewName", "profile");
        // Flag to indicate this page requires authentication
        model.addAttribute("requiresAuth", true);
        return "layout/main";
    }

    /**
     * Renders the settings page.
     *
     * @param model The Spring UI Model.
     * @return The name of the settings view template.
     */
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        model.addAttribute("title", "DevSpace - Settings");
        model.addAttribute("currentPage", "settings");
        // Flag to indicate this page requires authentication
        model.addAttribute("requiresAuth", true);
        return "redirect:/profile#settings";
    }

    @RestController
    @RequestMapping("/api/user")
    public class UserRestController {

        private final UserService userService;
        private final JWTUtil jwtUtil;
        private final OnlineUserService onlineUserService;
        private final CustomUserDetailsService userDetailsService;
        
        @Autowired
        public UserRestController(UserService userService, JWTUtil jwtUtil, 
                                OnlineUserService onlineUserService, 
                                CustomUserDetailsService userDetailsService) {
            this.userService = userService;
            this.jwtUtil = jwtUtil;
            this.onlineUserService = onlineUserService;
            this.userDetailsService = userDetailsService;
        }

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
                    
                    // 更新安全上下文中的认证信息
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null) {
                        // 可选：更新安全上下文中的认证信息
                        // 这里不实现，因为当前请求结束后上下文会被清除，而新请求会使用新的cookie
                    }
                } catch (Exception e) {
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
            String avatarUrl = userService.updateUserAvatar(avatar);
            return ResVo.ok(avatarUrl);
        }
        
        /**
         * 获取当前用户资料接口
         */
        @GetMapping("/profile")
        @TraceLog("获取用户资料")
        public ResVo<UserVO> getUserProfile() {
            Long userId = getCurrentUserId();
            UserDO user = userService.getById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            UserVO vo = new UserVO();
            org.springframework.beans.BeanUtils.copyProperties(user, vo);
            return ResVo.ok(vo);
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
} 
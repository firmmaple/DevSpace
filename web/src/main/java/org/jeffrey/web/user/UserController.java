package org.jeffrey.web.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.dto.user.UserProfileDTO;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.StatusEnum;
import org.jeffrey.api.vo.UserAvatarVO;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.security.CustomUserDetails;
import org.jeffrey.service.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器 - 统一处理用户资料和头像
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取当前用户的个人资料
     */
    @GetMapping("/profile")
    @TraceLog("获取个人资料")
    public ResVo<UserProfileDTO> getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResVo.fail(StatusEnum.FORBID_NOTLOGIN);
        }
        
        UserProfileDTO profile = userService.getUserProfile(userDetails.getUserId());
        if (profile == null) {
            return ResVo.fail(StatusEnum.USER_NOT_EXISTS, userDetails.getUserId());
        }
        
        return ResVo.ok(profile);
    }
    
    /**
     * 更新当前用户的个人资料
     * 支持同时更新基本信息和头像
     */
    @PostMapping("/profile")
    @TraceLog("更新个人资料")
    public ResVo<UserProfileDTO> updateUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        
        if (userDetails == null) {
            return ResVo.fail(StatusEnum.FORBID_NOTLOGIN);
        }
        
        try {
            // 构建个人资料对象
            UserProfileDTO profileDTO = UserProfileDTO.builder()
                    .id(userDetails.getUserId())
                    .username(username)
                    .email(email)
                    .bio(bio)
                    .build();
            
            // 更新个人资料
            UserProfileDTO updatedProfile = userService.updateUserProfile(userDetails.getUserId(), profileDTO, avatar);
            if (updatedProfile == null) {
                return ResVo.fail(StatusEnum.USER_NOT_EXISTS, userDetails.getUserId());
            }
            
            return ResVo.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            log.warn("更新个人资料失败: {}", e.getMessage());
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, e.getMessage());
        } catch (Exception e) {
            log.error("更新个人资料异常: ", e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "更新个人资料失败");
        }
    }
    
    /**
     * 单独获取用户头像信息
     */
    @GetMapping("/avatar")
    @TraceLog("获取用户头像")
    public ResVo<UserAvatarVO> getUserAvatar(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResVo.fail(StatusEnum.FORBID_NOTLOGIN);
        }
        
        String avatarUrl = userService.getUserAvatar(userDetails.getUserId());
        
        UserAvatarVO avatarVO = UserAvatarVO.builder()
                .userId(userDetails.getUserId())
                .avatarUrl(avatarUrl)
                .build();
        
        return ResVo.ok(avatarVO);
    }
    
    /**
     * 单独更新用户头像
     */
    @PostMapping("/avatar")
    @TraceLog("上传用户头像")
    public ResVo<UserAvatarVO> uploadAvatar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("avatar") MultipartFile avatar) {
        
        if (userDetails == null) {
            return ResVo.fail(StatusEnum.FORBID_NOTLOGIN);
        }
        
        if (avatar == null || avatar.isEmpty()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "头像文件不能为空");
        }
        
        // 上传头像
        String avatarUrl = userService.uploadAvatar(userDetails.getUserId(), avatar);
        if (avatarUrl == null) {
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "头像上传失败");
        }
        
        // 返回成功结果
        UserAvatarVO avatarVO = UserAvatarVO.builder()
                .userId(userDetails.getUserId())
                .avatarUrl(avatarUrl)
                .build();
        
        return ResVo.ok(avatarVO);
    }
} 
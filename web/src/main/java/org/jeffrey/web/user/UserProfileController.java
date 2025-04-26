package org.jeffrey.web.user;

import lombok.RequiredArgsConstructor;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.User.UserVO;
import org.jeffrey.service.article.service.ArticleService;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户主页控制器 - 用于访问其他用户的主页
 */
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final ArticleService articleService;

    /**
     * 查看用户主页
     *
     * @param userId 用户ID
     * @param model Spring UI Model
     * @return 视图模板名称
     */
    @GetMapping("/{userId}")
    public String userProfilePage(@PathVariable Long userId, Model model) {
        // 设置页面属性
        model.addAttribute("title", "DevSpace - User Profile");
        model.addAttribute("currentPage", "user");
        model.addAttribute("viewName", "user/profile");
        
        // 添加用户ID到模型中，页面上通过JavaScript获取用户详情
        model.addAttribute("userId", userId);
        
        return "layout/main";
    }
} 
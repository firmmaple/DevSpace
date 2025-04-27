package org.jeffrey.web.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 用户个人资料页面控制器
 */
@Controller
@RequiredArgsConstructor
public class ProfileController {

    /**
     * 渲染用户个人资料页面
     *
     * @param model Spring UI Model
     * @return 视图模板名称
     */
    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("title", "DevSpace - Profile");
        model.addAttribute("currentPage", "profile");
        model.addAttribute("viewName", "profile");
        // 标记此页面需要认证
        model.addAttribute("requiresAuth", true);
        return "layout/main";
    }

    /**
     * 查看用户主页
     *
     * @param userId 用户ID
     * @param model Spring UI Model
     * @return 视图模板名称
     */
    @GetMapping("/profile/{userId}")
    public String userProfilePage(@PathVariable Long userId, Model model) {
        // 设置页面属性
        model.addAttribute("title", "DevSpace - User Profile");
        model.addAttribute("currentPage", "user");
        model.addAttribute("viewName", "user/profile");

        // 添加用户ID到模型中，页面上通过JavaScript获取用户详情
        model.addAttribute("userId", userId);

        return "layout/main";
    }


    /**
     * 重定向到设置页面（设置标签页）
     *
     * @param model Spring UI Model
     * @return 重定向URL
     */
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        model.addAttribute("title", "DevSpace - Settings");
        model.addAttribute("currentPage", "settings");
        // 标记此页面需要认证
        model.addAttribute("requiresAuth", true);
        return "redirect:/profile#settings";
    }
} 
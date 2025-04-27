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
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final ArticleService articleService;

}
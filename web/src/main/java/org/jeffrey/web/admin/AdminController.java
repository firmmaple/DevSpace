package org.jeffrey.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.vo.Article.ArticleSummaryVO;
import org.jeffrey.service.article.repository.entity.ArticleDO;
import org.jeffrey.service.article.service.ArticleService;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ArticleService articleService;

    @GetMapping
    public String adminDashboard(Model model) {
        return redirectToUsers(model);
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        // Get all users
        List<UserDO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        
        // Set page properties
        model.addAttribute("title", "DevSpace Admin - Users");
        model.addAttribute("currentPage", "admin");
        model.addAttribute("adminSection", "users");
        model.addAttribute("viewName", "admin/users-management");
        
        return "layout/main";
    }

    @GetMapping("/articles")
    public String listArticles(Model model) {
        // Get all articles - page 1 with 100 items, no filters
        IPage<ArticleSummaryVO> articlesPage = articleService.listArticles(1, 100, null, null);
        model.addAttribute("articles", articlesPage.getRecords());
        
        // Set page properties
        model.addAttribute("title", "DevSpace Admin - Articles");
        model.addAttribute("currentPage", "admin");
        model.addAttribute("adminSection", "articles");
        model.addAttribute("viewName", "admin/articles-management");
        
        return "layout/main";
    }
    
    private String redirectToUsers(Model model) {
        // Set page properties and redirect to users by default
        model.addAttribute("title", "DevSpace Admin");
        model.addAttribute("currentPage", "admin");
        model.addAttribute("viewName", "admin/users-management");
        return "redirect:/admin/users";
    }
} 
package org.jeffrey.web.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.jeffrey.api.vo.Article.ArticleSummaryVO;
import org.jeffrey.service.article.service.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ArticleService articleService;

    /**
     * Renders the home page.
     *
     * @param model The Spring UI Model.
     * @return The name of the home view template.
     */
    @GetMapping("/")
    public String index(Model model) {
        // Set page attributes for the new layout
        model.addAttribute("title", "DevSpace - Home");
        model.addAttribute("currentPage", "home");
        model.addAttribute("viewName", "index");
        
        // 获取热门文章
        IPage<ArticleSummaryVO> hotArticlesPage = articleService.getHotArticles(1, 6);
        List<ArticleSummaryVO> topArticles = hotArticlesPage.getRecords();

        // 获取最新发布的文章（已发布状态的文章）
        IPage<ArticleSummaryVO> recentArticlesPage = articleService.listArticles(1, 10, null, 1);
        List<ArticleSummaryVO> recentArticles = recentArticlesPage.getRecords();
        
        model.addAttribute("topArticles", topArticles); // 使用从数据库获取的热门文章
        model.addAttribute("recentArticles", recentArticles); // 使用从数据库获取的最新文章
        return "layout/main"; // 使用主布局作为视图
    }

    /**
     * Redirects to the home page.
     *
     * @return Redirect to the home page.
     */
    @GetMapping("/home")
    public String redirectToHome() {
        return "redirect:/";
    }

    /**
     * Renders the about page.
     *
     * @param model The Spring UI Model.
     * @return The name of the about view template.
     */
    @GetMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("title", "DevSpace - About");
        model.addAttribute("currentPage", "about");
        model.addAttribute("viewName", "about");
        return "layout/main";
    }
} 
package org.jeffrey.web.article;

import lombok.RequiredArgsConstructor;
import org.jeffrey.api.vo.Article.ArticleVO;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.article.service.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for article pages
 */
@Controller
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    /**
     * Show article list page
     */
    @GetMapping
    @TraceLog("访问文章列表页面")
    public String listArticles(Model model, @RequestParam(required = false) String keyword) {
        model.addAttribute("title", "Articles - DevSpace");
        model.addAttribute("currentPage", "articles");
        model.addAttribute("viewName", "articles/list");

        // 如果有搜索关键词，传递给前端
        if (StringUtils.hasText(keyword)) {
            model.addAttribute("keyword", keyword);
        }

        return "layout/main";
    }

    /**
     * Show article detail page
     */
    @GetMapping("/{id}")
    @TraceLog("访问文章")
    public String viewArticle(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Article - DevSpace");
        model.addAttribute("currentPage", "articles");
        model.addAttribute("viewName", "articles/detail");
        return "layout/main";
    }

    /**
     * Show article create page
     */
    @GetMapping("/create")
    @TraceLog("访问创建文章页面")
    public String createArticle(Model model) {
        model.addAttribute("title", "Create Article - DevSpace");
        model.addAttribute("currentPage", "articles");
        model.addAttribute("viewName", "articles/create");
        return "layout/main";
    }

    /**
     * Show article edit page
     */
    @GetMapping("/edit/{id}")
    @TraceLog("访问编辑文章页面")
    public String editArticle(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Edit Article - DevSpace");
        model.addAttribute("currentPage", "articles");
        model.addAttribute("viewName", "articles/create"); // Reuse the create page for editing
        ArticleVO article = articleService.getArticleById(id, null); // This should fetch the article details
        model.addAttribute("article", article);

        return "layout/main";
    }
} 
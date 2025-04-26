package org.jeffrey.web.article;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.jeffrey.api.dto.article.ArticleCreateDTO;
import org.jeffrey.api.dto.article.ArticleUpdateDTO;
import org.jeffrey.api.vo.Article.ArticleSummaryVO;
import org.jeffrey.api.vo.Article.ArticleVO;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.ai.service.AIService;
import org.jeffrey.service.article.service.ArticleService;
import org.jeffrey.service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleRestController {
    private final ArticleService articleService;
    private final AIService aiService;

    @PostMapping
//    @PreAuthorize("isAuthenticated()") // Ensure user is logged in
    @TraceLog("发布新文章API")
    public ResVo<ArticleVO> createArticle(@RequestBody ArticleCreateDTO createDTO,
                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long authorId = currentUser.getUserId();
        createDTO.setSummary(aiService.getArticleSummary(createDTO.getContent()));
        ArticleVO createdArticle = articleService.createArticle(createDTO, authorId);
        return ResVo.ok(createdArticle);
    }

    @GetMapping("/{id}")
    @TraceLog("获取文章详情API")
    public ResVo<ArticleVO> getArticle(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long currentUserId = (currentUser != null) ? currentUser.getUserId() : null;
        ArticleVO article = articleService.getArticleById(id, currentUserId);
        // Handle not found in service layer via exception, caught by global handler
        return ResVo.ok(article);
    }

    @PutMapping("/{id}")
//    @PreAuthorize("isAuthenticated()")
    @TraceLog("更新文章API")
    public ResVo<ArticleVO> updateArticle(@PathVariable Long id,
                                          @RequestBody ArticleUpdateDTO updateDTO,
                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        ArticleVO updatedArticle = articleService.updateArticle(id, updateDTO, currentUser.getUserId());
        return ResVo.ok(updatedArticle);
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("isAuthenticated()")
    @TraceLog("删除文章API")
    public ResVo<String> deleteArticle(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        articleService.deleteArticle(id, currentUser.getUserId());
        return ResVo.ok(); // Return success status
    }

    @GetMapping
    @TraceLog("获取文章列表API")
    public ResVo<IPage<ArticleSummaryVO>> listArticles(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long authorId, // Optional filter by author
            @RequestParam(required = false) Integer status // Optional filter by status (e.g., for drafts)
    ) {
        IPage<ArticleSummaryVO> page = articleService.listArticles(pageNum, pageSize, authorId, status);
        return ResVo.ok(page);
    }

    // --- Interaction Endpoints ---
    @PostMapping("/{id}/like")
    @TraceLog("点赞文章API")
    public ResVo<String> likeArticle(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
        articleService.likeArticle(id, currentUser.getUserId());
        return ResVo.ok();
    }

    @DeleteMapping("/{id}/like")
    @TraceLog("取消点赞API")
    public ResVo<String> unlikeArticle(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        articleService.unlikeArticle(id, currentUser.getUserId());
        return ResVo.ok();
    }

    @PostMapping("/{id}/collect")
    @TraceLog("收藏文章API")
    public ResVo<String> collectArticle(@PathVariable Long id,
                                     @AuthenticationPrincipal CustomUserDetails currentUser) {
        articleService.collectArticle(id, currentUser.getUserId());
        return ResVo.ok();
    }

    @DeleteMapping("/{id}/collect")
    @TraceLog("取消收藏API")
    public ResVo<String> uncollectArticle(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        articleService.uncollectArticle(id, currentUser.getUserId());
        return ResVo.ok();
    }
}

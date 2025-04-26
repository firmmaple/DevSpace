package org.jeffrey.web.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.StatusEnum;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.article.service.ArticleService;
import org.jeffrey.service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户收藏文章相关的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserCollectionController {

    private final ArticleService articleService;

    /**
     * 获取当前用户收藏的文章列表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param currentUser 当前用户
     * @return 分页结果
     */
    @GetMapping("/collections")
    @TraceLog("获取用户收藏列表")
    public ResVo<?> getUserCollections(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        if (currentUser == null) {
            return ResVo.fail(StatusEnum.FORBID_NOTLOGIN);
        }
        
        try {
            Long userId = currentUser.getUserId();
            return ResVo.ok(articleService.getUserCollectedArticles(userId, pageNum, pageSize));
        } catch (Exception e) {
            log.error("获取用户收藏列表失败", e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "获取用户收藏列表失败: " + e.getMessage());
        }
    }
}

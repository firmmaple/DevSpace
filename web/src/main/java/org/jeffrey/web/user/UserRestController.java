package org.jeffrey.web.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.StatusEnum;
import org.jeffrey.api.vo.User.UserVO;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.article.service.ArticleService;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息REST API控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final ArticleService articleService;

    /**
     * 获取用户详细信息
     *
     * @param userId 用户ID
     * @return 用户信息和统计数据
     */
    @GetMapping("/{userId}")
    @TraceLog("获取用户详情API")
    public ResVo<Map<String, Object>> getUserInfo(@PathVariable Long userId) {
        try {
            // 获取用户基本信息
            UserDO user = userService.getById(userId);
            if (user == null) {
                return ResVo.fail(StatusEnum.USER_NOT_EXISTS);
            }
            
            // 转换为视图对象
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            
            // 获取用户统计数据（发表文章数，获赞数等）
            long articleCount = articleService.countArticlesByAuthorId(userId);
            long likeCount = articleService.countArticleLikesByAuthorId(userId);
            long collectCount = articleService.countArticleCollectsByAuthorId(userId);
            
            // 组合结果
            Map<String, Object> result = new HashMap<>();
            result.put("user", userVO);
            result.put("stats", Map.of(
                "articleCount", articleCount,
                "likeCount", likeCount,
                "collectCount", collectCount
            ));
            
            return ResVo.ok(result);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "获取用户信息失败: " + e.getMessage());
        }
    }
} 
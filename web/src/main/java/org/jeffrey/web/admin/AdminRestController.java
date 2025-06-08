package org.jeffrey.web.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.StatusEnum;
import org.jeffrey.api.vo.User.UserVO;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.article.service.ArticleService;
import org.jeffrey.service.user.service.UserService;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private final UserService userService;
    private final ArticleService articleService;

    @TraceLog("管理员删除用户")
    @DeleteMapping("/users/{userId}")
    public ResVo<Boolean> deleteUser(@PathVariable String userId) {
        try {
            Long id = Long.parseLong(userId);
            boolean result = userService.deleteUserById(id);
            if (result) {
                return ResVo.ok(true);
            } else {
                return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "删除用户失败");
            }
        } catch (NumberFormatException e) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS, "用户ID格式不正确");
        } catch (Exception e) {
            log.error("删除用户失败: " + userId, e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "删除用户失败: " + e.getMessage());
        }
    }

    @TraceLog("管理员删除文章")
    @DeleteMapping("/articles/{articleId}")
    public ResVo<Boolean> deleteArticle(@PathVariable String articleId) {
        try {
            Long id = Long.parseLong(articleId);
            boolean result = articleService.deleteArticleById(id);
            if (result) {
                return ResVo.ok(true);
            } else {
                return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "删除文章失败");
            }
        } catch (NumberFormatException e) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS, "文章ID格式不正确");
        } catch (Exception e) {
            log.error("删除文章失败: " + articleId, e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "删除文章失败: " + e.getMessage());
        }
    }

    @TraceLog("管理员获取用户详情")
    @GetMapping("/users/{userId}")
    public ResVo<UserVO> getUserDetail(@PathVariable String userId) {
        try {
            Long id = Long.parseLong(userId);
            UserVO user = userService.getUserDetailById(id);
            if (user != null) {
                return ResVo.ok(user);
            } else {
                return ResVo.fail(StatusEnum.USER_NOT_EXISTS, "用户不存在");
            }
        } catch (NumberFormatException e) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS, "用户ID格式不正确");
        } catch (Exception e) {
            log.error("获取用户详情失败: " + userId, e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "获取用户详情失败: " + e.getMessage());
        }
    }

    @TraceLog("管理员切换用户管理员权限")
    @PutMapping("/users/{userId}/admin")
    public ResVo<Boolean> toggleAdminStatus(@PathVariable String userId, @RequestParam Boolean isAdmin) {
        try {
            Long id = Long.parseLong(userId);
            UserDO user = userService.getById(id);
            if (user == null) {
                return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS, "用户不存在");
            }
            
            user.setIsAdmin(isAdmin);
            boolean result = userService.updateById(user);
            
            if (result) {
                return ResVo.ok(true);
            } else {
                return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "更新用户管理员权限失败");
            }
        } catch (NumberFormatException e) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS, "用户ID格式不正确");
        } catch (Exception e) {
            log.error("更新用户管理员权限失败: " + userId, e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "更新用户管理员权限失败: " + e.getMessage());
        }
    }

    @TraceLog("管理员切换文章推荐状态")
    @PutMapping("/articles/{articleId}/recommended")
    public ResVo<Boolean> toggleRecommendedStatus(@PathVariable String articleId, @RequestParam Boolean isRecommended) {
        try {
            Long id = Long.parseLong(articleId);
            boolean result = articleService.toggleArticleRecommendedStatus(id, isRecommended);
            if (result) {
                return ResVo.ok(true);
            } else {
                return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "更新文章推荐状态失败");
            }
        } catch (NumberFormatException e) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS, "文章ID格式不正确");
        } catch (Exception e) {
            log.error("更新文章推荐状态失败: " + articleId, e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "更新文章推荐状态失败: " + e.getMessage());
        }
    }
} 
package org.jeffrey.web.article;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.jeffrey.api.dto.interaction.CommentDTO;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.comment.CommentVO;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.article.service.CommentService;
import org.jeffrey.service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentRestController {
    private final CommentService commentService;

    @PostMapping
    @TraceLog("添加评论API")
    public ResVo<CommentVO> addComment(@RequestBody CommentDTO commentDTO,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        commentDTO.setUserId(currentUser.getUserId());
        CommentVO comment = commentService.addComment(commentDTO);
        return ResVo.ok(comment);
    }
    
    @DeleteMapping("/{id}")
    @TraceLog("删除评论API")
    public ResVo<String> deleteComment(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetails currentUser) {
        commentService.deleteComment(id, currentUser.getUserId());
        return ResVo.ok();
    }
    
    @GetMapping("/article/{articleId}")
    @TraceLog("获取文章评论API")
    public ResVo<IPage<CommentVO>> getArticleComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<CommentVO> comments = commentService.getArticleComments(articleId, pageNum, pageSize);
        return ResVo.ok(comments);
    }
} 
package org.jeffrey.service.article.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.jeffrey.api.dto.interaction.CommentDTO;
import org.jeffrey.api.exception.ResourceNotFoundException;
import org.jeffrey.api.vo.comment.CommentVO;
import org.jeffrey.core.event.ArticleCommentEvent;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.article.repository.entity.ArticleDO;
import org.jeffrey.service.article.repository.entity.CommentDO;
import org.jeffrey.service.article.repository.mapper.ArticleMapper;
import org.jeffrey.service.article.repository.mapper.CommentMapper;
import org.jeffrey.service.article.service.CommentService;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @TraceLog("添加评论")
    public CommentVO addComment(CommentDTO commentDTO) {
        // Check if article exists
        ArticleDO article = articleMapper.selectById(commentDTO.getArticleId());
        if (article == null || article.getStatus() == 2) {
            throw new ResourceNotFoundException("Article not found with ID: " + commentDTO.getArticleId());
        }

        // If this is a reply, check if parent comment exists
        if (commentDTO.getParentId() != null) {
            CommentDO parentComment = commentMapper.selectById(commentDTO.getParentId());
            if (parentComment == null) {
                throw new ResourceNotFoundException("Parent comment not found with ID: " + commentDTO.getParentId());
            }
        }

        // Publish event to handle comment asynchronously
        eventPublisher.publishEvent(new ArticleCommentEvent(
                this,
                commentDTO.getArticleId(),
                commentDTO.getUserId(),
                commentDTO.getContent(),
                commentDTO.getParentId()
        ));

        // Create a response VO (without DB ID since processing is async)
        CommentVO commentVO = new CommentVO();
        commentVO.setArticleId(commentDTO.getArticleId());
        commentVO.setUserId(commentDTO.getUserId());
        commentVO.setContent(commentDTO.getContent());
        commentVO.setParentId(commentDTO.getParentId());

        // Get user info
        UserDO user = userService.getById(commentDTO.getUserId());
        commentVO.setUsername(user != null ? user.getUsername() : "Unknown");
        commentVO.setAvatarUrl(user != null ? user.getAvatarUrl() : null);

        return commentVO;
    }

    @Override
    @TraceLog("删除评论")
    public void deleteComment(Long commentId, Long userId) {
        CommentDO comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return; // Nothing to delete or throw exception
        }

        // Authorization check - only the comment author can delete
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("Only the comment author can delete their comments");
            // Consider using a proper exception like ForbiddenAccessException
        }

        // Delete the comment and all its replies
        commentMapper.deleteById(commentId);

        // Find and delete all replies
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CommentDO> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CommentDO::getParentId, commentId);
        commentMapper.delete(wrapper);
    }

    @Override
    @TraceLog("获取文章评论")
    public List<CommentVO> getArticleComments(Long articleId) {
        List<CommentVO> allComments = commentMapper.findByArticleId(articleId)
                .stream()
                .map(this::convertToVO)
                .toList();

        List<CommentVO> rootComments = new ArrayList<>();
        Map<Long, CommentVO> commentMap = new HashMap<>();

        for (CommentVO comment : allComments) {
            commentMap.put(comment.getId(), comment);
        }

        for (CommentVO comment : allComments) {
            UserDO user = userService.getById(comment.getUserId());
            comment.setUsername(user != null ? user.getUsername() : "Unknown");
            comment.setAvatarUrl(user != null ? user.getAvatarUrl() : null);
            if (commentMap.get(comment.getParentId()) != null) {
                commentMap.get(comment.getParentId()).getReplies().add(comment);
            } else {
                rootComments.add(comment);
            }
        }

        return rootComments;
    }


    @Override
    @TraceLog("获取文章评论（分页，但是只能嵌套1个")
    public IPage<CommentVO> getArticleComments(Long articleId, int pageNum, int pageSize) {
        // Get top-level comments with pagination
        Page<CommentDO> page = new Page<>(pageNum, pageSize);
        IPage<CommentDO> commentPage = commentMapper.findByArticleIdAndParentIdIsNull(page, articleId);

        // Convert to VO and enrich with user info and replies
        IPage<CommentVO> result = commentPage.convert(this::convertToVO);

        // Get all user IDs to fetch user info in batch
        List<Long> userIds = new ArrayList<>();
        result.getRecords().forEach(comment -> userIds.add(comment.getUserId()));

        // Get parent comment IDs to fetch replies
        List<Long> parentIds = result.getRecords().stream()
                .map(CommentVO::getId)
                .collect(Collectors.toList());

        // Fetch replies for all parent comments
        if (!parentIds.isEmpty()) {
            // Group replies by parent ID
            Map<Long, List<CommentDO>> repliesByParent = parentIds.stream()
                    .flatMap(parentId -> commentMapper.findByParentId(parentId).stream())
                    .collect(Collectors.groupingBy(CommentDO::getParentId));

            // Add comment author IDs to the list for batch fetch
            repliesByParent.values().stream()
                    .flatMap(List::stream)
                    .map(CommentDO::getUserId)
                    .forEach(userIds::add);

            // Fetch all users in batch
            Map<Long, UserDO> usersById = userService.getUsersByIds(userIds).stream()
                    .collect(Collectors.toMap(UserDO::getId, user -> user));

            // Populate replies and user info
            result.getRecords().forEach(comment -> {
                // Set username
                UserDO user = usersById.get(comment.getUserId());
                comment.setUsername(user != null ? user.getUsername() : "Unknown");
                comment.setAvatarUrl(user != null ? user.getAvatarUrl() : null);

                // Set replies
                List<CommentDO> replies = repliesByParent.get(comment.getId());
                if (replies != null && !replies.isEmpty()) {
                    List<CommentVO> replyVOs = replies.stream()
                            .map(reply -> {
                                CommentVO replyVO = convertToVO(reply);
                                UserDO replyUser = usersById.get(reply.getUserId());
                                replyVO.setUsername(replyUser != null ? replyUser.getUsername() : "Unknown");
                                replyVO.setAvatarUrl(replyUser != null ? replyUser.getAvatarUrl() : null);
                                return replyVO;
                            })
                            .collect(Collectors.toList());
                    comment.setReplies(replyVOs);
                }
            });
        }

        return result;
    }

    @Override
    @TraceLog("获取文章评论数")
    public Long getArticleCommentCount(Long articleId) {
        return commentMapper.countByArticleId(articleId);
    }

    private CommentVO convertToVO(CommentDO commentDO) {
        if (commentDO == null) return null;

        CommentVO vo = new CommentVO();
        vo.setId(commentDO.getId());
        vo.setArticleId(commentDO.getArticleId());
        vo.setUserId(commentDO.getUserId());
        vo.setContent(commentDO.getContent());
        vo.setParentId(commentDO.getParentId());
        vo.setCreatedAt(commentDO.getCreatedAt());
        vo.setReplies(new ArrayList<>());

        return vo;
    }
} 
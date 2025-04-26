package org.jeffrey.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import org.jeffrey.api.exception.ResourceNotFoundException;
import org.jeffrey.api.vo.Article.ArticleSummaryVO;
import org.jeffrey.api.vo.Article.ArticleVO;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.core.util.MarkdownUtil;
import org.jeffrey.service.article.repository.entity.ArticleDO;
import org.jeffrey.service.article.service.ArticleService;
import org.jeffrey.service.article.repository.mapper.ArticleMapper;
import org.jeffrey.api.dto.article.*;
// Import other necessary services (like UserService to get username)
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.jeffrey.core.event.ArticleCollectEvent;
import org.jeffrey.core.event.ArticleLikeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.jeffrey.service.article.repository.mapper.ArticleLikeMapper;
import org.jeffrey.service.article.repository.mapper.ArticleCollectMapper;
// Import exceptions, user service, etc.
import java.util.Arrays;
import java.util.List;
import org.jeffrey.service.article.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.service.article.service.ArticleViewCountService;


@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    final private ArticleMapper articleMapper;
    final private UserService userService; // Inject user service to get author details
    final private ApplicationEventPublisher eventPublisher;
    final private ArticleLikeMapper likeMapper;
    final private ArticleCollectMapper collectMapper;
    final private CommentService commentService; // 添加CommentService依赖
    final private ArticleViewCountService viewCountService; // 添加ArticleViewCountService依赖

    @Override
    @TraceLog("创建文章") // Use your AOP logging
    public ArticleVO createArticle(ArticleCreateDTO createDTO, Long authorId) {
        ArticleDO articleDO = new ArticleDO();
        // BeanUtils.copyProperties(createDTO, articleDO); // Or manual mapping
        articleDO.setTitle(createDTO.getTitle());
        articleDO.setSummary(createDTO.getSummary());
        articleDO.setContent(createDTO.getContent());
        articleDO.setAuthorId(authorId);
        articleDO.setStatus(0); // Default to draft
        articleMapper.insert(articleDO);
        // Convert DO to VO, fetch author username etc.
        return convertToVO(articleDO, null); // Pass null for currentUserId initially
    }

    @Override
    @TraceLog("获取文章详情")
    public ArticleVO getArticleById(Long articleId, Long currentUserId) {
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (articleDO == null || articleDO.getStatus() == 2) { // Check for deleted status
            // Throw custom exception or return null/fail ResVo
            throw new ResourceNotFoundException("Article not found with ID: " + articleId);
        }
        
        // Increment view count for this article
        Long viewCount = viewCountService.incrementViewCount(articleId);
        
        ArticleVO articleVO = convertToVO(articleDO, currentUserId);
        // Set view count from Redis
        articleVO.setViewCount(viewCount);
        
        return articleVO;
    }

    @Override
    @TraceLog("更新文章")
    public ArticleVO updateArticle(Long articleId, ArticleUpdateDTO updateDTO, Long currentUserId) {
        ArticleDO existingArticle = articleMapper.selectById(articleId);
        if (existingArticle == null || existingArticle.getStatus() == 2) {
            throw new ResourceNotFoundException("Article not found");
        }
        // Authorization Check: Ensure the current user is the author
//        if (!existingArticle.getAuthorId().equals(currentUserId)) {
//            throw new ForbiddenAccessException("You are not authorized to update this article");
//        }
        // Update fields from DTO
        existingArticle.setTitle(updateDTO.getTitle());
        // ... update other fields ...
        articleMapper.updateById(existingArticle);
        return convertToVO(existingArticle, currentUserId);
    }

    @Override
    @TraceLog("删除文章")
    public void deleteArticle(Long articleId, Long currentUserId) {
        ArticleDO existingArticle = articleMapper.selectById(articleId);
        if (existingArticle == null) return; // Or throw exception

//        if (!existingArticle.getAuthorId().equals(currentUserId)) {
//            throw new ForbiddenAccessException("You are not authorized to delete this article");
//        }
        // Soft delete: Update status to 'deleted'
        existingArticle.setStatus(2);
        articleMapper.updateById(existingArticle);
        // Or Hard delete: articleMapper.deleteById(articleId);
    }

    @Override
    @TraceLog("获取文章列表")
    public IPage<ArticleSummaryVO> listArticles(int pageNum, int pageSize, Long filterAuthorId, Integer filterStatus) {
        Page<ArticleDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<>();

        // Default to only showing published articles unless specified otherwise
        wrapper.eq(filterStatus != null, ArticleDO::getStatus, filterStatus)
                .eq(filterStatus == null, ArticleDO::getStatus, 1); // Default to published (status=1)

        wrapper.eq(filterAuthorId != null, ArticleDO::getAuthorId, filterAuthorId);
        wrapper.orderByDesc(ArticleDO::getCreatedAt); // Sort by creation time

        IPage<ArticleDO> articlePage = articleMapper.selectPage(page, wrapper);

        // Convert Page<ArticleDO> to Page<ArticleSummaryVO>
        return articlePage.convert(articleDO -> convertToSummaryVO(articleDO));
    }

    @Override
    @TraceLog("点赞文章")
    public void likeArticle(Long articleId, Long userId) {
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (articleDO == null || articleDO.getStatus() == 2) {
            throw new ResourceNotFoundException("Article not found with ID: " + articleId);
        }
        
        // Publish event - asynchronous processing
        eventPublisher.publishEvent(new ArticleLikeEvent(this, articleId, userId, true));
    }

    @Override
    @TraceLog("取消点赞")
    public void unlikeArticle(Long articleId, Long userId) {
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (articleDO == null || articleDO.getStatus() == 2) {
            throw new ResourceNotFoundException("Article not found with ID: " + articleId);
        }
        
        // Publish event - asynchronous processing
        eventPublisher.publishEvent(new ArticleLikeEvent(this, articleId, userId, false));
    }

    @Override
    @TraceLog("收藏文章")
    public void collectArticle(Long articleId, Long userId) {
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (articleDO == null || articleDO.getStatus() == 2) {
            throw new ResourceNotFoundException("Article not found with ID: " + articleId);
        }
        
        // Publish event - asynchronous processing
        eventPublisher.publishEvent(new ArticleCollectEvent(this, articleId, userId, true));
    }

    @Override
    @TraceLog("取消收藏")
    public void uncollectArticle(Long articleId, Long userId) {
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (articleDO == null || articleDO.getStatus() == 2) {
            throw new ResourceNotFoundException("Article not found with ID: " + articleId);
        }
        
        // Publish event - asynchronous processing
        eventPublisher.publishEvent(new ArticleCollectEvent(this, articleId, userId, false));
    }

    @Override
    public boolean isArticleLikedByUser(Long articleId, Long userId) {
        if (userId == null) return false;
        return likeMapper.existsByArticleIdAndUserId(articleId, userId);
    }

    @Override
    public boolean isArticleCollectedByUser(Long articleId, Long userId) {
        if (userId == null) return false;
        return collectMapper.existsByArticleIdAndUserId(articleId, userId);
    }

    @Override
    public Long getArticleLikeCount(Long articleId) {
        return likeMapper.countByArticleId(articleId);
    }

    @Override
    public Long getArticleCollectCount(Long articleId) {
        return collectMapper.countByArticleId(articleId);
    }

    // 获取文章评论数
    private Long getArticleCommentCount(Long articleId) {
        // 调用CommentService获取评论数
        try {
            return commentService.getArticleCommentCount(articleId);
        } catch (Exception e) {
            log.error("Error getting comment count for article {}: {}", articleId, e.getMessage());
            return 0L; // 默认返回0
        }
    }
    
    // 获取文章标签
    private List<String> getArticleTags(Long articleId) {
        // 这里应该是从数据库中获取文章标签的实现
        // 如简单起见，暂时返回一些示例标签
        return Arrays.asList("Java", "Spring Boot", "Web Development");
    }

    // --- Helper Methods ---
    private ArticleVO convertToVO(ArticleDO articleDO, Long currentUserId) {
        if (articleDO == null) return null;
        ArticleVO vo = new ArticleVO();
        vo.setId(articleDO.getId());
        vo.setTitle(articleDO.getTitle());
        vo.setSummary(articleDO.getSummary());
        
        // 如果文章内容是Markdown格式，转换为HTML
        String rawContent = articleDO.getContent();
        String htmlContent = MarkdownUtil.convertToHtml(rawContent);
        vo.setContent(htmlContent);
        
        // 保存原始Markdown内容（用于编辑）
        vo.setRawContent(rawContent);
        
        vo.setAuthorId(articleDO.getAuthorId());
        vo.setStatus(articleDO.getStatus());
        vo.setCreatedAt(articleDO.getCreatedAt());
        vo.setUpdatedAt(articleDO.getUpdatedAt());

        // Fetch and set author information
        UserDO author = userService.getById(articleDO.getAuthorId()); // Assuming you have this method
        if (author != null) {
            vo.setAuthorUsername(author.getUsername());
            vo.setAuthorAvatarUrl(author.getAvatarUrl());  // 设置作者头像URL
            vo.setAuthorBio(author.getBio());  // 设置作者简介
        } else {
            vo.setAuthorUsername("Unknown");
        }
        
        // Get view count from the view count service
        Long viewCount = viewCountService.getViewCount(articleDO.getId());
        vo.setViewCount(viewCount);
        
        // Get interaction counts and status
        vo.setLikeCount(getArticleLikeCount(articleDO.getId()));
        vo.setCollectCount(getArticleCollectCount(articleDO.getId()));
        vo.setCommentCount(getArticleCommentCount(articleDO.getId()));
        
        // Set current user's interaction status
        if (currentUserId != null) {
            vo.setLikedByCurrentUser(isArticleLikedByUser(articleDO.getId(), currentUserId));
            vo.setCollectedByCurrentUser(isArticleCollectedByUser(articleDO.getId(), currentUserId));
        }
        
        // Get tags
        vo.setTags(getArticleTags(articleDO.getId()));
        
        return vo;
    }

    private ArticleSummaryVO convertToSummaryVO(ArticleDO articleDO) {
        if (articleDO == null) return null;
        ArticleSummaryVO summaryVO = new ArticleSummaryVO();
        // BeanUtils.copyProperties(articleDO, summaryVO, "content"); // Exclude content
        summaryVO.setId(articleDO.getId());
        summaryVO.setTitle(articleDO.getTitle());
        summaryVO.setSummary(articleDO.getSummary());
        summaryVO.setAuthorId(articleDO.getAuthorId());
        summaryVO.setCreatedAt(articleDO.getCreatedAt());
        summaryVO.setStatus(articleDO.getStatus());

        UserDO author = userService.getById(articleDO.getAuthorId());
        summaryVO.setAuthorUsername(author != null ? author.getUsername() : "Unknown");

        // Get view count from view count service
        summaryVO.setViewCount(viewCountService.getViewCount(articleDO.getId()));
        // Get interaction counts
        summaryVO.setLikeCount(getArticleLikeCount(articleDO.getId()));
        summaryVO.setCollectCount(getArticleCollectCount(articleDO.getId()));

        return summaryVO;
    }

}

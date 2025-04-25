package org.jeffrey.core.event;

import lombok.Getter;

/**
 * Event for article comment interactions
 */
@Getter
public class ArticleCommentEvent extends ArticleInteractionEvent {
    private final String content;
    private final Long parentId; // For reply comments, null for top-level comments
    
    public ArticleCommentEvent(Object source, Long articleId, Long userId, String content, Long parentId) {
        super(source, articleId, userId, true); // Comments are always additions
        this.content = content;
        this.parentId = parentId;
    }
} 
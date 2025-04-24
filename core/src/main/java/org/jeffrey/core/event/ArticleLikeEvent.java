package org.jeffrey.core.event;

import lombok.Getter;

/**
 * Event for article like interactions
 */
@Getter
public class ArticleLikeEvent extends ArticleInteractionEvent {
    public ArticleLikeEvent(Object source, Long articleId, Long userId, boolean isAdd) {
        super(source, articleId, userId, isAdd);
    }
} 
package org.jeffrey.core.event;

import lombok.Getter;

/**
 * Event for article collect interactions
 */
@Getter
public class ArticleCollectEvent extends ArticleInteractionEvent {
    public ArticleCollectEvent(Object source, Long articleId, Long userId, boolean isAdd) {
        super(source, articleId, userId, isAdd);
    }
} 
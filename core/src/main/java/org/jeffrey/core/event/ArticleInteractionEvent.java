package org.jeffrey.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Base class for article interaction events
 */
@Getter
public abstract class ArticleInteractionEvent extends ApplicationEvent {
    private final Long articleId;
    private final Long userId;
    private final boolean isAdd; // true for add interaction, false for remove

    public ArticleInteractionEvent(Object source, Long articleId, Long userId, boolean isAdd) {
        super(source);
        this.articleId = articleId;
        this.userId = userId;
        this.isAdd = isAdd;
    }
} 
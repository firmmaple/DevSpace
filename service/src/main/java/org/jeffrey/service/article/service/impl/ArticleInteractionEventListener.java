package org.jeffrey.service.article.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.dto.interaction.CollectDTO;
import org.jeffrey.api.dto.interaction.CommentDTO;
import org.jeffrey.api.dto.interaction.LikeDTO;
import org.jeffrey.core.event.ArticleCollectEvent;
import org.jeffrey.core.event.ArticleCommentEvent;
import org.jeffrey.core.event.ArticleLikeEvent;
import org.jeffrey.core.mq.MQPublisher;
import org.jeffrey.core.mq.RabbitMQConfig;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for Spring events and sends them to RabbitMQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleInteractionEventListener {
    private final MQPublisher mqPublisher;

    @Async
    @EventListener
    public void handleArticleLikeEvent(ArticleLikeEvent event) {
        log.info("Received article like event: articleId={}, userId={}, isAdd={}", 
                event.getArticleId(), event.getUserId(), event.isAdd());
        
        LikeDTO likeDTO = new LikeDTO(event.getArticleId(), event.getUserId(), event.isAdd());
        mqPublisher.sendMessage(
                RabbitMQConfig.INTERACTION_EXCHANGE,
                RabbitMQConfig.LIKE_ROUTING_KEY, 
                likeDTO
        );
    }
    
    @Async
    @EventListener
    public void handleArticleCollectEvent(ArticleCollectEvent event) {
        log.info("Received article collect event: articleId={}, userId={}, isAdd={}", 
                event.getArticleId(), event.getUserId(), event.isAdd());
        
        CollectDTO collectDTO = new CollectDTO(event.getArticleId(), event.getUserId(), event.isAdd());
        mqPublisher.sendMessage(
                RabbitMQConfig.INTERACTION_EXCHANGE,
                RabbitMQConfig.COLLECT_ROUTING_KEY, 
                collectDTO
        );
    }
    
    @Async
    @EventListener
    public void handleArticleCommentEvent(ArticleCommentEvent event) {
        log.info("Received article comment event: articleId={}, userId={}, content={}", 
                event.getArticleId(), event.getUserId(), event.getContent());
        
        CommentDTO commentDTO = new CommentDTO(
                event.getArticleId(), 
                event.getUserId(), 
                event.getContent(), 
                event.getParentId()
        );
        mqPublisher.sendMessage(
                RabbitMQConfig.INTERACTION_EXCHANGE,
                RabbitMQConfig.COMMENT_ROUTING_KEY, 
                commentDTO
        );
    }
} 
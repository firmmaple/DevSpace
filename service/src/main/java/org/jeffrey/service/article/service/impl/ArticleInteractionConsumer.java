package org.jeffrey.service.article.service.impl;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.dto.interaction.CollectDTO;
import org.jeffrey.api.dto.interaction.CommentDTO;
import org.jeffrey.api.dto.interaction.LikeDTO;
import org.jeffrey.core.mq.RabbitMQConfig;
import org.jeffrey.service.article.repository.entity.ArticleCollectDO;
import org.jeffrey.service.article.repository.entity.ArticleLikeDO;
import org.jeffrey.service.article.repository.entity.CommentDO;
import org.jeffrey.service.article.repository.mapper.ArticleCollectMapper;
import org.jeffrey.service.article.repository.mapper.ArticleLikeMapper;
import org.jeffrey.service.article.repository.mapper.ArticleMapper;
import org.jeffrey.service.article.repository.mapper.CommentMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Consumes messages from RabbitMQ for article interactions
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleInteractionConsumer {
    private final ArticleMapper articleMapper;
    private final ArticleLikeMapper likesMapper;
    private final ArticleCollectMapper collectMapper;
    private final CommentMapper commentMapper;

    @RabbitListener(queues = RabbitMQConfig.LIKE_QUEUE)
    public void handleLikeMessage(@Payload LikeDTO likeDTO,
                                  Message message,
                                  Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received like message: {}", likeDTO);
        try {
            if (likeDTO.isAdd()) {
                // Check if already liked
                if (!likesMapper.existsByArticleIdAndUserId(likeDTO.getArticleId(), likeDTO.getUserId())) {
                    // Add like
                    ArticleLikeDO likeDO = new ArticleLikeDO();
                    likeDO.setArticleId(likeDTO.getArticleId());
                    likeDO.setUserId(likeDTO.getUserId());
                    likeDO.setCreatedAt(LocalDateTime.now());
                    likesMapper.insert(likeDO);
                    
                    // Could update article like count in article table for faster retrieval
                    // articleMapper.incrementLikeCount(likeDTO.getArticleId());
                }
            } else {
                // Remove like
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ArticleLikeDO> wrapper = 
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                wrapper.eq(ArticleLikeDO::getArticleId, likeDTO.getArticleId())
                       .eq(ArticleLikeDO::getUserId, likeDTO.getUserId());
                likesMapper.delete(wrapper);
                
                // Could update article like count in article table for faster retrieval
                // articleMapper.decrementLikeCount(likeDTO.getArticleId());
            }
            
            // Acknowledge message
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing like message: {}", e.getMessage(), e);
            // Reject message and don't requeue
            channel.basicReject(tag, false);
        }
    }
    
    @RabbitListener(queues = RabbitMQConfig.COLLECT_QUEUE)
    public void handleCollectMessage(@Payload CollectDTO collectDTO,
                                    Message message,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received collect message: {}", collectDTO);
        try {
            if (collectDTO.isAdd()) {
                // Check if already collected
                if (!collectMapper.existsByArticleIdAndUserId(collectDTO.getArticleId(), collectDTO.getUserId())) {
                    // Add collect
                    ArticleCollectDO collectDO = new ArticleCollectDO();
                    collectDO.setArticleId(collectDTO.getArticleId());
                    collectDO.setUserId(collectDTO.getUserId());
                    collectDO.setCreatedAt(LocalDateTime.now());
                    collectMapper.insert(collectDO);
                    
                    // Could update article collect count in article table for faster retrieval
                    // articleMapper.incrementCollectCount(collectDTO.getArticleId());
                }
            } else {
                // Remove collect
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ArticleCollectDO> wrapper = 
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                wrapper.eq(ArticleCollectDO::getArticleId, collectDTO.getArticleId())
                       .eq(ArticleCollectDO::getUserId, collectDTO.getUserId());
                collectMapper.delete(wrapper);
                
                // Could update article collect count in article table for faster retrieval
                // articleMapper.decrementCollectCount(collectDTO.getArticleId());
            }
            
            // Acknowledge message
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing collect message: {}", e.getMessage(), e);
            // Reject message and don't requeue
            channel.basicReject(tag, false);
        }
    }
    
    @RabbitListener(queues = RabbitMQConfig.COMMENT_QUEUE)
    public void handleCommentMessage(@Payload CommentDTO commentDTO,
                                    Message message,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received comment message: {}", commentDTO);
        try {
            // Add comment
            CommentDO commentDO = new CommentDO();
            commentDO.setArticleId(commentDTO.getArticleId());
            commentDO.setUserId(commentDTO.getUserId());
            commentDO.setContent(commentDTO.getContent());
            commentDO.setParentId(commentDTO.getParentId());
            commentDO.setCreatedAt(LocalDateTime.now());
            commentDO.setUpdatedAt(LocalDateTime.now());
            commentMapper.insert(commentDO);
            
            // Could update article comment count in article table for faster retrieval
            // articleMapper.incrementCommentCount(commentDTO.getArticleId());
            
            // Acknowledge message
            // System.out.println("Comment message acknowledged");
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing comment message: {}", e.getMessage(), e);
            // Reject message and don't requeue
            channel.basicReject(tag, false);
        }
    }
} 
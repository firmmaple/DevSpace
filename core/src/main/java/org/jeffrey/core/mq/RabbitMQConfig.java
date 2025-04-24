package org.jeffrey.core.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Exchange names
    public static final String INTERACTION_EXCHANGE = "interaction.exchange";
    
    // Queue names
    public static final String LIKE_QUEUE = "article.like.queue";
    public static final String COLLECT_QUEUE = "article.collect.queue";
    public static final String COMMENT_QUEUE = "article.comment.queue";
    
    // Routing keys
    public static final String LIKE_ROUTING_KEY = "article.like";
    public static final String COLLECT_ROUTING_KEY = "article.collect";
    public static final String COMMENT_ROUTING_KEY = "article.comment";
    
    @Bean
    public TopicExchange interactionExchange() {
        return new TopicExchange(INTERACTION_EXCHANGE);
    }
    
    @Bean
    public Queue likeQueue() {
        return new Queue(LIKE_QUEUE, true);
    }
    
    @Bean
    public Queue collectQueue() {
        return new Queue(COLLECT_QUEUE, true);
    }
    
    @Bean
    public Queue commentQueue() {
        return new Queue(COMMENT_QUEUE, true);
    }
    
    @Bean
    public Binding likeBinding(Queue likeQueue, TopicExchange interactionExchange) {
        return BindingBuilder.bind(likeQueue).to(interactionExchange).with(LIKE_ROUTING_KEY);
    }
    
    @Bean
    public Binding collectBinding(Queue collectQueue, TopicExchange interactionExchange) {
        return BindingBuilder.bind(collectQueue).to(interactionExchange).with(COLLECT_ROUTING_KEY);
    }
    
    @Bean
    public Binding commentBinding(Queue commentQueue, TopicExchange interactionExchange) {
        return BindingBuilder.bind(commentQueue).to(interactionExchange).with(COMMENT_ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
} 
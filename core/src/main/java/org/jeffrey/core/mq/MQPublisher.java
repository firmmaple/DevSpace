package org.jeffrey.core.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MQPublisher {
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * Send a message to the specified exchange with the given routing key
     * 
     * @param exchange the exchange to publish to
     * @param routingKey the routing key
     * @param message the message to send
     */
    public void sendMessage(String exchange, String routingKey, Object message) {
        log.info("Sending message to exchange: {}, routing key: {}, message: {}", exchange, routingKey, message);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
} 
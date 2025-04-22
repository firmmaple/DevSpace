package org.jeffrey.core;

import org.jeffrey.core.cache.RedisClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@ComponentScan("org.jeffrey.core")
@EnableAspectJAutoProxy
public class CoreAutoConfig {
    public CoreAutoConfig(RedisTemplate<String, String> redisTemplate) {
        RedisClient.register(redisTemplate);
    }
}

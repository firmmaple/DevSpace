package org.jeffrey.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan("org.jeffrey.service")
@MapperScan(basePackageClasses = {
        org.jeffrey.service.user.repository.mapper.UserMapper.class,
        org.jeffrey.service.article.repository.mapper.ArticleMapper.class,
        org.jeffrey.service.article.repository.mapper.ArticleViewCountMapper.class,
})
@EnableScheduling
public class ServiceAutoConfig {
}

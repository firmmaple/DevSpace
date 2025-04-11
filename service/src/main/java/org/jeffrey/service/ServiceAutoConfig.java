package org.jeffrey.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.jeffrey.service")
@MapperScan(basePackageClasses = {
        org.jeffrey.service.user.repository.mapper.UserMapper.class,
})
public class ServiceAutoConfig {
}

package org.jeffrey.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类，用于配置静态资源映射等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加对uploads目录的访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // 添加专门用于头像的映射
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:uploads/avatars/");
    }
}
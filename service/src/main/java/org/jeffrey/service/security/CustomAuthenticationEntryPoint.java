package org.jeffrey.service.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.net.URLEncoder;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final HandlerExceptionResolver resolver;


    public CustomAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // 检查是否是API请求
        // 一般API请求会有这些特征之一：Accept头包含application/json或URL以/api/开头
        boolean isApiRequest = request.getHeader("Accept") != null &&
                request.getHeader("Accept").contains("application/json") ||
                request.getRequestURI().startsWith("/api/") ||
                request.getHeader("X-Requested-With") != null &&
                        request.getHeader("X-Requested-With").equals("XMLHttpRequest");

        if (isApiRequest) {
            // API请求由GlobalExceptionHandler处理，返回JSON
            this.resolver.resolveException(request, response, null, authException);
        } else {
            // 页面请求重定向到登录页
            log.info("AuthenticationEntryPoint抛出异常");
//            response.sendRedirect("/login?redirect=" + URLEncoder.encode(request.getRequestURI(), "UTF-8"));
            response.sendRedirect("/login?redirect=" + request.getRequestURI());
        }


    }
}

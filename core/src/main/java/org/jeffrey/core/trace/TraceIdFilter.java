package org.jeffrey.core.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TraceID过滤器
 * 在每个HTTP请求中设置TraceID
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // 检查请求头中是否已有TraceID
            String traceId = request.getHeader(TraceConstants.TRACE_ID_HEADER);
            
            // 如果请求头中没有TraceID，则生成新的
            if (traceId == null || traceId.isEmpty()) {
                traceId = TraceUtil.generateTraceId();
            } else {
                TraceUtil.setTraceId(traceId);
            }
            
            // 将TraceID添加到响应头
            response.setHeader(TraceConstants.TRACE_ID_HEADER, traceId);
            
            // 继续过滤器链
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清除TraceID
            TraceUtil.clearTraceId();
        }
    }
} 
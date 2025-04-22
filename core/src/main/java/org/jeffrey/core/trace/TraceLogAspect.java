package org.jeffrey.core.trace;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 跟踪日志切面
 * 记录被@TraceLog注解标记的方法的访问日志
 */
@Aspect
@Component
@Slf4j
public class TraceLogAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定义切点：所有被@TraceLog注解标记的方法
     */
    @Pointcut("@annotation(org.jeffrey.core.trace.TraceLog) || @within(org.jeffrey.core.trace.TraceLog)")
    public void traceLogPointcut() {
    }

    /**
     * 环绕通知：记录方法执行前后的日志
     */
    @Around("traceLogPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 获取类名和方法名
        String className = point.getTarget().getClass().getName();
        String methodName = method.getName();

        // 获取方法上的TraceLog注解
        TraceLog traceLog = method.getAnnotation(TraceLog.class);
        // 如果方法上没有，则获取类上的TraceLog注解
        if (traceLog == null) {
            traceLog = point.getTarget().getClass().getAnnotation(TraceLog.class);
        }

        // 操作说明
        String operation = traceLog != null ? traceLog.value() : "";

        // TraceID
        String traceId = TraceUtil.getTraceId();

        // 请求参数
        String params = "";
        if (traceLog != null && traceLog.recordParams()) {
            try {
                params = objectMapper.writeValueAsString(point.getArgs());
            } catch (Exception e) {
                params = "无法序列化的参数";
            }
            // 记录请求日志（包含请求参数）
            log.info("开始执行 - {}.{} - {} - 参数: {}",
                    className, methodName, operation, params);
        } else {
            log.info("开始执行 - {}.{} - {}",
                    className, methodName, operation);

        }


        if (request != null) {
            log.info("请求信息 - 请求URI: {} - 请求方法: {} - 客户端IP: {}",
                    request.getRequestURI(), request.getMethod(), getClientIp(request));
        }

        // 执行原方法
        Object result = null;
        try {
            result = point.proceed();
            return result;
        } catch (Throwable throwable) {
            // 记录异常日志
            // log.error("执行异常 - {}.{} - 异常: {}",
            //      className, methodName, throwable.getMessage(), throwable);
            throw throwable;
        } finally {
            // 执行时间
            long timeCost = System.currentTimeMillis() - startTime;

            // 记录响应日志
            if (traceLog != null && traceLog.recordResult() && result != null) {
                String resultStr;
                try {
                    resultStr = objectMapper.writeValueAsString(result);
                    if (resultStr.length() > 1000) {
                        resultStr = resultStr.substring(0, 1000) + "... (内容过长已截断)";
                    }
                } catch (Exception e) {
                    resultStr = "无法序列化的结果";
                }

                log.info("执行完成 - {}.{} - 耗时: {}ms - 结果: {}",
                        className, methodName, timeCost, resultStr);
            } else {
                log.info("执行完成 - {}.{} - 耗时: {}ms",
                        className, methodName, timeCost);
            }
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
} 
package org.jeffrey.core.trace.demo;

import lombok.extern.slf4j.Slf4j;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.core.trace.TraceUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * TraceLog示例控制器
 * 仅用于演示如何使用TraceLog注解
 */
@RestController
@RequestMapping("/api/trace-demo")
@Slf4j
public class TraceLogDemoController {

    /**
     * 简单示例
     */
    @GetMapping("/hello")
    @TraceLog("访问Hello接口")
    public Map<String, Object> hello(@RequestParam(required = false) String name) {
        log.info("处理hello请求，当前TraceID: {}", TraceUtil.getTraceId());
        
        // 调用服务方法
        String message = demoService(name != null ? name : "World");
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("traceId", TraceUtil.getTraceId());
        return result;
    }
    
    /**
     * 示例服务方法
     */
    @TraceLog(value = "内部服务方法", recordResult = true)
    private String demoService(String name) {
        log.info("执行服务方法，当前TraceID: {}", TraceUtil.getTraceId());
        return "Hello, " + name + "!";
    }
} 
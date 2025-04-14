package org.jeffrey.core.trace;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceID工具类
 */
public class TraceUtil {
    
    /**
     * 生成TraceID并存入MDC
     * @return 生成的TraceID
     */
    public static String generateTraceId() {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TraceConstants.TRACE_ID_KEY, traceId);
        return traceId;
    }
    
    /**
     * 获取当前TraceID
     * @return 当前TraceID，若不存在则生成新的
     */
    public static String getTraceId() {
        String traceId = MDC.get(TraceConstants.TRACE_ID_KEY);
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        return traceId;
    }
    
    /**
     * 设置TraceID到MDC
     * @param traceId TraceID
     */
    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TraceConstants.TRACE_ID_KEY, traceId);
        }
    }
    
    /**
     * 清除MDC中的TraceID
     */
    public static void clearTraceId() {
        MDC.remove(TraceConstants.TRACE_ID_KEY);
    }
} 
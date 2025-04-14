package org.jeffrey.core.trace;

/**
 * 跟踪常量类
 */
public class TraceConstants {
    /**
     * TraceID在MDC中的键名
     */
    public static final String TRACE_ID_KEY = "traceId";
    
    /**
     * 请求头中的TraceID名称
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
} 
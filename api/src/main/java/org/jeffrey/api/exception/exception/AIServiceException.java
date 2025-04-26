package org.jeffrey.api.exception.exception;

/**
 * 表示AI服务调用过程中发生的异常
 */
public class AIServiceException extends RuntimeException {
    
    public AIServiceException(String message) {
        super(message);
    }
    
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
} 
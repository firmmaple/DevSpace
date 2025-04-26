package org.jeffrey.api.exception.exception;

/**
 * 表示AI服务配置错误导致的异常
 */
public class AIConfigurationException extends AIServiceException {
    
    public AIConfigurationException(String message) {
        super(message);
    }
    
    public AIConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
} 
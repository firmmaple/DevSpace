package org.jeffrey.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.StatusEnum;
import org.jeffrey.core.trace.TraceUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResVo<String> handleException(Exception e, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.error("全局异常处理 [traceId={}]: {}", traceId, e.getMessage(), e);
        return ResVo.fail(StatusEnum.UNEXPECT_ERROR, e.getMessage());
    }

    /**
     * 处理参数校验异常 (方法参数校验)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVo<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMsg = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常: {}", errorMsg);
        return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, errorMsg);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVo<String> handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMsg = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数绑定异常: {}", errorMsg);
        return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, errorMsg);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVo<String> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMsg = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("约束违反异常: {}", errorMsg);
        return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, errorMsg);
    }

    /**
     * 处理请求参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVo<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("请求参数缺失: {}", e.getMessage());
        return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "缺少必要参数: " + e.getParameterName());
    }

    /**
     * 处理请求体解析异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVo<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析异常: {}", e.getMessage());
        return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS, "请求体格式不正确");
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResVo<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {}", e.getMessage());
        return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, 
                "参数 '" + e.getName() + "' 类型不匹配，需要 " + e.getRequiredType().getSimpleName());
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResVo<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());
        return ResVo.fail(StatusEnum.FORBID_ERROR_MIXED, "不支持 " + e.getMethod() + " 请求方法");
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResVo<String> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return ResVo.fail(StatusEnum.FORBID_ERROR);
    }

    /**
     * 处理认证异常
     * TODO 前端添加登陆失败提示
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResVo<String> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证异常: {}", e.getMessage());
        if (e instanceof BadCredentialsException) {
            return ResVo.fail(StatusEnum.USER_PWD_ERROR);
        }else if(e instanceof UsernameNotFoundException){
            return ResVo.fail(StatusEnum.USER_NOT_EXISTS);
        }
        return ResVo.fail(StatusEnum.FORBID_NOTLOGIN);
    }
} 
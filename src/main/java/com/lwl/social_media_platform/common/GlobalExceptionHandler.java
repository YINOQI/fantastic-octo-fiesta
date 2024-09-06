package com.lwl.social_media_platform.common;


import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.lwl.social_media_platform.common.exception.AbstractException;
import com.lwl.social_media_platform.common.exception.LoginException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理Token过期或未登录异常
     *
     * @param ex ex
     * @return Result
     */
    @ExceptionHandler
    public Result<String> tokenExceptionHandler(TokenExpiredException ex) {
        return Result.error(ex.getMessage());
    }

    /**
     * 处理Token过期或未登录异常
     *
     * @param mes mes
     * @return Result
     */
    @ExceptionHandler(value = LoginException.class)
    public Result<String> tokenExceptionHandler(LoginException mes) {
        return Result.error(mes.getMessage());
    }

    @ExceptionHandler(value = AbstractException.class)
    public Result<String> abstractException(AbstractException mes) {
        return Result.error(mes.getErrorMessage());
    }

    /**
     * 捕获SQL异常
     *
     * @param ex ex
     * @return Result
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username = split[2];
            return Result.error(username + "已存在！");
        }else {
            return Result.error("未知错误！");
        }
    }

    @ExceptionHandler(value = Throwable.class)
    public Result<String> defaultErrorHandler(HttpServletRequest request, Throwable throwable) {
        log.error("[{}] {} ", request.getMethod(), getUrl(request), throwable);
        return Result.error("");
    }

    private String getUrl(HttpServletRequest request) {
        if (StrUtil.isEmpty(request.getQueryString())) {
            return request.getRequestURL().toString();
        }
        return request.getRequestURL().toString() + "?" + request.getQueryString();
    }
}

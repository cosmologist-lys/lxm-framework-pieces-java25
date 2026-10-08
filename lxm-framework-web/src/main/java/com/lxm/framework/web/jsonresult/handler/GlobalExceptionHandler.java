package com.lxm.framework.web.jsonresult.handler;

import com.lxm.framework.common.AppException;
import com.lxm.framework.common.auth.errs.AbstractAuthException;
import com.lxm.framework.web.jsonresult.JsonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Author: Lys
 * @Date 2022/3/7
 * @Describe
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public JsonResult<?> allExceptions(HttpServletRequest req, HttpServletResponse res, Exception e) {
        log.error("Unhandled request failure at {}", req.getRequestURI(), e);
        res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return JsonResult.json(-1, "服务器暂时无法处理请求");
    }

    @ExceptionHandler(AppException.class)
    public JsonResult<?> handleAppException(HttpServletRequest req, HttpServletResponse res, AppException e) {
        log.debug("global exception handler ,uri :{} , app-exception : {}", req.getRequestURI(), e);
        res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return JsonResult.json(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(AbstractAuthException.class)
    public JsonResult<?> handleAuthException(HttpServletRequest req, HttpServletResponse res, AbstractAuthException e) {
        log.debug("global exception handler ,uri :{} , app-exception : {}", req.getRequestURI(), e.getMessage());
        if (1001 == e.getCode()) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
        } else if (1002 == e.getCode()) {
            res.setStatus(HttpStatus.FORBIDDEN.value());
        } else {
            res.setStatus(HttpStatus.OK.value());
        }
        return JsonResult.json(e.getCode(), e.getMessage());
    }


    @ExceptionHandler({org.springframework.web.bind.MethodArgumentNotValidException.class,
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class})
    public JsonResult<?> invalidRequest(HttpServletResponse response, Exception failure) {
        response.setStatus(400);
        return JsonResult.json(400, "请求参数不正确");
    }

}

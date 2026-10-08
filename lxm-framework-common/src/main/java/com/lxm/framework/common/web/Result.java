package com.lxm.framework.common.web;

import com.lxm.framework.common.errs.BaseError;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author: Lys
 * @Date 2023/1/9
 * @Describe
 **/
public final class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T returnValue;

    private Result() {
        super();
    }

    private Result(Integer code, String message, T returnValue) {
        this.code = code;
        this.message = message;
        this.returnValue = returnValue;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Boolean success() {
        return code != null && code == 0;
    }

    public Boolean failed() {
        return !success();
    }

    public T getReturnValue() {
        return returnValue;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReturnValue(T returnValue) {
        this.returnValue = returnValue;
    }

    public static <T> Result<T> of() {
        return of(0, "", null);
    }

    public static <T> Result<T> ofWithFailed() {
        return of(-1, "", null);
    }

    public static <T> Result<T> ofWithFailed(T returnValue) {
        return of(-1, "", returnValue);
    }

    public static <T> Result<T> of(T returnValue) {
        return of(0, "", returnValue);
    }

    public static <T> Result<T> of(int code, String message) {
        return of(code, message, null);
    }

    public static <T> Result<T> of(int code, String message, T returnValue) {
        return new Result<>(code, message, returnValue);
    }

    public static <T> Result<T> of(BaseError error, T returnValue) {
        return new Result<>(error.getCode(), error.getMessage(), returnValue);
    }

    public static <T> Result<T> of(BaseError error) {
        return of(error.getCode(), error.getMessage());
    }

    /*public static Result<IPage> of(IPage page) {
        return of(0, "", page);
    }*/

    public static Result<Map<String, Object>> ofMap(Map<String, Object> map) {
        int code = (int) map.getOrDefault("code", 1);
        String message = (String) map.getOrDefault("message", "");
        return new Result<>(code, message, map);
    }

    @Override
    public String toString() {
        return "Result [code="
                + code
                + ", message="
                + message
                + ", returnValue="
                + returnValue
                + "]";
    }
}

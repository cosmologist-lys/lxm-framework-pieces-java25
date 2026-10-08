package com.lxm.framework.common;

import com.lxm.framework.common.errs.CommonError;

/**
 * @Author: Lys
 * @Date 2023/1/9
 * @Describe
 **/
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 2301567098448502500L;

    private final int code;

    public int getCode() {
        return this.code;
    }

    public AppException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AppException(int code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }

    public AppException(CommonError error) {
        super(error.getMessage());
        this.code = error.getCode();
    }
}

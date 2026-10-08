package com.lxm.framework.common.auth.errs;

import com.lxm.framework.common.errs.BaseError;

/**
 * @Author: Lys
 * @Date 2023/6/8
 * @Describe
 **/
public class AbstractAuthException extends RuntimeException {

    private static final long serialVersionUID = 6703320214272357686L;
    private final int code;
    private final String message;

    public AbstractAuthException(BaseError error) {
        super(error.getMessage());
        this.code = error.getCode();
        this.message = error.getMessage();
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

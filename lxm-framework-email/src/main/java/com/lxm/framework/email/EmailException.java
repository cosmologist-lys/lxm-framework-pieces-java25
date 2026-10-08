package com.lxm.framework.email;

/**
 * @Author: Lys
 * @Date 2022/6/23
 * @Describe
 **/
public class EmailException extends RuntimeException {
    private static final long serialVersionUID = 3127526127829866639L;

    public EmailException() {
    }

    public EmailException(String message) {
        super(message);
    }

    public EmailException(Throwable cause) {
        super(cause);
    }
}

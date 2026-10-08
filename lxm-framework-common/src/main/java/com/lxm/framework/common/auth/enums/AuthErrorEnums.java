package com.lxm.framework.common.auth.enums;

import com.lxm.framework.common.errs.BaseError;

/**
 * @Author: Lys
 * @Date 2023/1/10
 * @Describe
 **/
public enum AuthErrorEnums implements BaseError {
    OK(0, ""),
    NOT_LOGIN(1000, "未登录"),
    ACCOUNT_PASSWORD_NOT_MATCHED(1001, "账号或密码错误"),
    ACCOUNT_NOT_EXIST(1002, "未找到此账户"),
    DUP_ACCOUNT(1003, "已存在同名账号");

    private final int code;
    private final String message;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    AuthErrorEnums(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

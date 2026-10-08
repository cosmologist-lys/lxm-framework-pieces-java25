package com.lxm.framework.auth.enums;

import com.lxm.framework.common.errs.BaseError;
import lombok.Getter;

/**
 * @Author: Lys
 * @Date 2023/5/29
 * @Describe
 **/
public enum AuthPrompts implements BaseError {
    FAILED_FETCH_CONTEXT_REQUEST(8901, "非Web上下文无法获取Request"),
    FAILED_FETCH_CONTEXT_RESPONSE(8902, "非Web上下文无法获取Response"),
    NOT_SIGN_IN(1001, "尚未登录"),
    IS_FORBID(1002, "该账号处于封禁状态无法登录"),
    UNQUALIFIED_ROLE_OR_PERMISSION(2000, "该账号缺少操作或角色权限"),
    UNQUALIFIED_PERMISSION(2001, "该账号缺少对应操作权限"),
    UNQUALIFIED_ROLE(2002, "该账号缺少对应角色权限"),
    ;

    @Getter private final int code;

    @Getter private final String message;

    AuthPrompts(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

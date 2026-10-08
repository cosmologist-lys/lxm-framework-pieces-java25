package com.lxm.framework.common.errs;

/**
 * @Author: Lys
 * @Date 2023/1/9
 * @Describe
 **/
public enum CommonError implements BaseError {
    INVALID_PARAMS(-1, ""),
    NOT_FOUND_TARGET(-1, ""),
    DUPLICATED_KEY_PARAM(-1, ""),
    WEB_CLIENT_INVALID_PARAMS(-1, "lxm web-client initialized failed,invalid params"),
    SERVER_ERROR(500, ""),

    REDIS_INVALID_PARAMETERS(-1, "redis-session invalid parameters | 重要参数未设置，或操作顺序错误");

    private final int code;
    private final String message;

    CommonError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getMessage() {
        return null;
    }
}

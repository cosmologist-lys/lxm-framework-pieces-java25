package com.lxm.framework.redis.exceptions;

import com.lxm.framework.common.AppException;

/**
 * @Author: Lys
 * @Date 2023/5/9
 * @Describe
 **/
public class RedisLockerInvalidParamsException extends AppException {

    @Override
    public int getCode() {
        return -99223;
    }

    public RedisLockerInvalidParamsException() {
        super(
                -99223,
                "invalid params of lxm-redis-locker , maybe duration-time is greater than expire-time");
    }
}

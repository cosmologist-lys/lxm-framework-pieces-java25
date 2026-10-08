package com.lxm.framework.redis.exceptions;

import com.lxm.framework.common.AppException;

/**
 * @Author: Lys
 * @Date 2023/5/8
 * @Describe
 **/
public class RedisLockerOccupiedException extends AppException {

    private static final long serialVersionUID = 1568274016118567412L;

    @Override
    public int getCode() {
        return -99222;
    }

    public RedisLockerOccupiedException() {
        super(-99222, "lxm-redis-locker,current resource if occupied");
    }
}

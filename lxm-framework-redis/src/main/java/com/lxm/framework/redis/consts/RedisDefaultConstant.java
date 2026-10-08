package com.lxm.framework.redis.consts;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Lys
 * @Date 2023/5/6
 * @Describe
 **/
public class RedisDefaultConstant {

    public static final String LISTENER_PREFIX = "redis_listener_";

    public static final Long DEFAULT_EXPIRE = 5000L;

    public static final Long DEFAULT_EXPIRE_MAX = 60_000L;

    public static final Long DEFAULT_DURATION = 500L;

    public static final Long DEFAULT_DURATION_MIN = 300L;

    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    public static final Long DEFAULT_MESSAGE_BUS_PUBLISHER_EXPIRE_MILLIS = 60 * 60 * 1000L;
}

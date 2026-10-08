package com.lxm.framework.redis.inf;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/5/18
 * @Describe
 **/
public interface LockerWrapperSession {

    /**
     * lock过期时间
     *
     * @param milliseconds
     * @return
     */
    default LockerWrapperSession expire(long milliseconds) {
        return expire(milliseconds, TimeUnit.MILLISECONDS);
    }

    LockerWrapperSession expire(long expire, TimeUnit timeUnit);

    /**
     * lock等待时间
     *
     * @param milliseconds
     * @return
     */
    default LockerWrapperSession duration(long milliseconds) {
        return duration(milliseconds, TimeUnit.MILLISECONDS);
    }

    LockerWrapperSession duration(long duration, TimeUnit timeUnit);
}

package com.lxm.framework.redis.parts;

import com.lxm.framework.redis.consts.RedisDefaultConstant;
import lombok.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/5/8
 * @Describe
 **/

public class LockerCore extends AbstractRedisPartCore {

    private static final long serialVersionUID = -3396506124740791159L;

    public LockerCore() {
        // 默认5s expire
        setExpire(RedisDefaultConstant.DEFAULT_EXPIRE);
        setExpireUnit(RedisDefaultConstant.DEFAULT_TIME_UNIT);
        // 默认0.5s duration
        setDuration(RedisDefaultConstant.DEFAULT_DURATION);
        setDurationUnit(RedisDefaultConstant.DEFAULT_TIME_UNIT);
    }

    private Long duration;

    private TimeUnit durationUnit;
    private TimeUnit expireUnit;

    @Override
    public LockerCore setExpire(@NonNull Long expire) {
        if (expire > RedisDefaultConstant.DEFAULT_EXPIRE_MAX || expire < 0L) {
            super.setExpire(RedisDefaultConstant.DEFAULT_EXPIRE_MAX);
        } else {
            super.setExpire(expire);
        }
        return this;
    }

    public TimeUnit getDurationUnit() {
        return durationUnit;
    }

    public LockerCore setDurationUnit(TimeUnit durationUnit) {
        this.durationUnit = durationUnit;
        return this;
    }

    public TimeUnit getExpireUnit() {
        return expireUnit;
    }

    public LockerCore setExpireUnit(TimeUnit expireUnit) {
        this.expireUnit = expireUnit;
        return this;
    }

    public Long getDuration() {
        return duration;
    }

    public LockerCore setDuration(Long duration) {
        if (duration < RedisDefaultConstant.DEFAULT_DURATION_MIN) {
            duration = RedisDefaultConstant.DEFAULT_DURATION;
        }
        this.duration = duration;
        return this;
    }


}

package com.lxm.framework.redis.impl;

import com.lxm.framework.common.AppException;
import com.lxm.framework.common.errs.CommonError;
import com.lxm.framework.redis.impl.locker.LockerImpl;
import com.lxm.framework.redis.inf.LockerSession;
import com.lxm.framework.redis.inf.LockerWrapperSession;
import com.lxm.framework.redis.parts.LockerCore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/5/5
 * @Describe
 **/
@Slf4j
public final class RedisLocker implements LockerWrapperSession {

    private final StringRedisTemplate session;
    private final LockerCore core;

    private RedisLocker(StringRedisTemplate session) {
        this.session = session;
        this.core = new LockerCore();
    }

    protected static RedisLocker of(StringRedisTemplate session) {
        return new RedisLocker(session);
    }

    public RedisLocker prefix(String prefix) {
        core.setPrefix(prefix);
        return this;
    }

    public RedisLocker key(String key) {
        core.setKey(key);
        return this;
    }

    public LockerSession use() {
        beforeUsing();
        return new LockerImpl(session, core);
    }

    private void beforeUsing() {
        if (StringUtils.isBlank(core.getFinKey())) {
            throw new AppException(CommonError.REDIS_INVALID_PARAMETERS);
        }
    }

    @Override
    public RedisLocker expire(long expire, TimeUnit timeUnit) {
        if (expire != core.getExpire()) {
            core.setExpire(expire);
        }
        if (null != timeUnit) {
            core.setExpireUnit(timeUnit);
        }
        return this;
    }

    @Override
    public RedisLocker duration(long duration, TimeUnit timeUnit) {
        if (duration != core.getDuration()) {
            core.setDuration(duration);
        }
        if (null != timeUnit) {
            core.setDurationUnit(timeUnit);
        }
        return this;
    }
}

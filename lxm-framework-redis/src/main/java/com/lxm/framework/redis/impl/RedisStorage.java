package com.lxm.framework.redis.impl;

import com.lxm.framework.common.AppException;
import com.lxm.framework.common.errs.CommonError;
import com.lxm.framework.redis.cache.ListenerCache;
import com.lxm.framework.redis.impl.storage.StorageCleanser;
import com.lxm.framework.redis.impl.storage.StorageImpl;
import com.lxm.framework.redis.inf.GenericSession;
import com.lxm.framework.redis.inf.KeyPrefixRule;
import com.lxm.framework.redis.inf.RedisOnRemoveListener;
import com.lxm.framework.redis.inf.StoreSession;
import com.lxm.framework.redis.parts.StoreCore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/2/7
 * @Describe
 **/
@Slf4j
public final class RedisStorage implements GenericSession {

    private final RedisTemplate session;
    private final StoreCore core;
    private StorageImpl storageImpl;

    private RedisStorage(RedisTemplate session) {
        this.session = session;
        this.core = new StoreCore();
        storageImpl = null;
    }

    public static RedisStorage of(RedisTemplate session) {
        return new RedisStorage(session);
    }

    private void parameters(String prefix, String key, Long expire, TimeUnit timeUnit, RedisOnRemoveListener listener) {
        core.setPrefix(prefix).setKey(key).setTimeUnit(timeUnit).setListener(listener);
        if (null != expire) {
            core.setExpire(expire);
        }
    }


    @Override
    public GenericSession key(String key) {
        if (null != core.getKey()) {
            core.setKey(key);
        }
        this.parameters(null, key, null, null, null);
        return this;
    }

    @Override
    public GenericSession expire(long time, TimeUnit timeUnit) {
        this.parameters(null, null, time, timeUnit, null);
        return this;
    }

    protected void beforeUsing() {
        if (StringUtils.isBlank(core.getFinKey())) {
            throw new AppException(CommonError.REDIS_INVALID_PARAMETERS);
        }
    }

    @Override
    public GenericSession persist() {
        beforeUsing();
        core.setPermanent(true);
        return this;
    }

    @Override
    public GenericSession prefix(String prefix) {
        parameters(prefix, null, null, null, null);
        return this;
    }

    @Override
    public GenericSession prefix(KeyPrefixRule prefixRule) {
        return GenericSession.super.prefix(prefixRule);
    }

    @Override
    public void deleteBatch() {
        String finKey = core.getFinKey();
        if (!finKey.endsWith("*")) {
            finKey = finKey + "*";
        }
        Set<String> keys = this.session.keys(finKey);
        if (null != keys) {
            var cleanser = new StorageCleanser(core);
            for (String key : keys) {
                Object value = session.opsForValue().get(core.getFinKey());
                cleanser.onRemove(core.getFinKey(), value);
            }
            this.session.delete(keys);
        }
    }

    @Override
    public StoreSession use() {
        this.storageImpl = new StorageImpl(session, core);
        return this.storageImpl;
    }

    @Override
    public void delete() {
        Object value = session.opsForValue().get(core.getFinKey());
        if (null != value) {
            var cleanser = new StorageCleanser(core);
            cleanser.onRemove(core.getFinKey(), value);
        }
        session.delete(core.getFinKey());
    }

    @Override
    public GenericSession listener(RedisOnRemoveListener redisOnRemoveListener) {
        parameters(null, null, null, null, redisOnRemoveListener);
        ListenerCache.putListener(core.getPrefix(), core.getKey(), redisOnRemoveListener);
        return this;
    }


}

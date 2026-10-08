package com.lxm.framework.redis.impl.storage;

import com.lxm.framework.common.AppException;
import com.lxm.framework.common.errs.CommonError;
import com.lxm.framework.common.utils.DateTimeUtils;
import com.lxm.framework.redis.inf.StoreSession;
import com.lxm.framework.redis.parts.StoreCore;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/5/18
 * @Describe
 **/
public class StorageImpl implements StoreSession {
    private final RedisTemplate session;
    private final StoreCore core;

    public StorageImpl(RedisTemplate session, StoreCore core) {
        this.session = session;
        this.core = core;
    }

    private void beforeUsing() {
        if (StringUtils.isBlank(core.getFinKey())) {
            throw new AppException(CommonError.REDIS_INVALID_PARAMETERS);
        }
    }

    @Override
    public boolean exist() {
        beforeUsing();
        var r = session.hasKey(getKey());
        return null != r && r;
    }

    @Override
    public long expire() {
        beforeUsing();
        Long expire = session.opsForValue().getOperations().getExpire(getKey());
        return null != expire ? expire : 0L;
    }

    @Override
    public void store(@NonNull Object value) {
        beforeUsing();
        core.setValue(value);
        if (core.isPermanent()) {
            session.opsForValue().set(getKey(), value);
        } else {
            session.opsForValue().set(getKey(), value, core.getExpire(), core.getTimeUnit());
        }
    }

    @Override
    public void store(@NonNull List<?> list) {
        beforeUsing();
        if (!core.isPermanent()) {
            session.expire(getKey(), core.getExpire(), core.getTimeUnit());
        }
        session.opsForList().rightPushAll(getKey(), list);
    }

    @Override
    public LocalDateTime expireAt() {
        beforeUsing();
        long expire = expire();
        return 0 == expire ? null : DateTimeUtils.millisToLocalTime(expire);
    }

    @Override
    public String getKey() {
        return core.getFinKey();
    }

    protected <T> T get(Class<T> tClass, boolean delete) {
        beforeUsing();
        Object value = delete ? session.opsForValue().getAndDelete(getKey()) : session.opsForValue().get(getKey());
        if (null != tClass && tClass.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    protected List<?> getList(boolean delete) {
        beforeUsing();
        var list = session.opsForList().range(getList(), 0, -1);
        if (delete) {
            session.delete(getKey());
        }
        return list;
    }

    @Override
    public <T> T get(Class<T> tClass) {
        return get(tClass, false);
    }

    @Override
    public <T> T get() {
        final Object value = session.opsForValue().get(getKey());
        if (null != value) {
            return (T) value;
        }
        return null;
    }

    @Override
    public <T> T getThenDelete(Class<T> tClass) {
        T value = get(tClass, true);
        var cleanser = new StorageCleanser(core);
        cleanser.onRemove(getKey(), value);
        return value;
    }

    @Override
    public List<?> getList() {
        return null;
    }

    @Override
    public List<?> getListThenDelete() {
        var list = getList(true);
        var cleanser = new StorageCleanser(core);
        cleanser.onRemove(getKey(), list);
        return list;
    }

}

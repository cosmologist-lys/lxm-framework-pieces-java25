package com.lxm.framework.redis.parts;

import com.lxm.framework.redis.inf.RedisOnRemoveListener;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/2/7
 * @Describe
 **/
public class StoreCore extends AbstractRedisPartCore {

    private static final long serialVersionUID = 1253084325054882553L;

    private Object value;
    private TimeUnit timeUnit;
    private Class<?> clazz;
    private RedisOnRemoveListener listener;
    private boolean permanent = false;

    public StoreCore() {
        // 默认24小时
        this.timeUnit = TimeUnit.HOURS;
        this.setExpire(24L);
    }

    public RedisOnRemoveListener getListener() {
        return listener;
    }

    public StoreCore setListener(RedisOnRemoveListener listener) {
        if (null != listener) {
            this.listener = listener;
        }
        return this;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public StoreCore setPermanent(boolean permanent) {
        this.permanent = permanent;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public StoreCore setValue(Object value) {
        this.value = value;
        if (null == clazz) {
            this.clazz = value.getClass();
        }
        return this;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public StoreCore setTimeUnit(TimeUnit timeUnit) {
        if (null != timeUnit) {
            this.timeUnit = timeUnit;
        }
        return this;
    }

    public Class<?> getClazz() {
        if (null == clazz) {
            if (null != value) {
                return value.getClass();
            }
        }
        return clazz;
    }

    public StoreCore setClazz(Class<?> clazz) {
        this.clazz = clazz;
        return this;
    }

    @Override
    public StoreCore setExpire(Long expire) {
        super.setExpire(expire);
        return this;
    }

    @Override
    public StoreCore setPrefix(String prefix) {
        super.setPrefix(prefix);
        return this;
    }

    @Override
    public StoreCore setKey(String key) {
        super.setKey(key);
        return this;
    }

    @Override
    public StoreCore setFinKey(String finKey) {
        super.setFinKey(finKey);
        return this;
    }
}

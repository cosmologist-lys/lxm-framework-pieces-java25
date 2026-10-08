package com.lxm.framework.common.cache.customized.parts;

import com.lxm.framework.common.utils.DateTimeUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: Lys
 * @Date 2023/1/13
 * @Describe
 **/
public class CacheTarget<K, V> implements Serializable {
    private static final long serialVersionUID = -3508190262824988314L;

    protected final K key;
    protected final V obj;
    /**
     * 上次访问时间
     */
    protected volatile long lastAccess;
    /**
     * 时间格式
     */
    protected final TimeUnit unit;
    /**
     * 访问次数
     */
    protected volatile AtomicLong accessCount = new AtomicLong();
    /**
     * 到期时间 local-date-time
     */
    private volatile LocalDateTime expireTime;
    /**
     * 到期时间 millis
     */
    private volatile long expireTimeMillis;
    /**
     * 是否永久存储
     */
    private volatile boolean permanent;
    /**
     * 过期时间的timestamp
     */
    private final long expire;

    public CacheTarget(K key, V obj, long expire) {
        this.expire = expire;
        this.key = key;
        this.obj = obj;
        this.unit = TimeUnit.SECONDS;
        this.lastAccess = System.currentTimeMillis();
        this.refresh();
    }

    public CacheTarget(K key, V obj, long expire, TimeUnit unit) {
        this.expire = expire;
        this.key = key;
        this.obj = obj;
        this.unit = unit;
        this.lastAccess = System.currentTimeMillis();
        this.refresh();
    }

    /**
     * 获取键，并且更新最后访问时间
     *
     * @return 键
     * @since 4.0.10
     */
    public K getKey() {
        lastAccess = System.currentTimeMillis();
        accessCount.getAndIncrement();
        return this.key;
    }


    /**
     * 获取值，并且更新最后访问时间
     *
     * @return 值
     * @since 4.0.10
     */
    public V getValue() {
        lastAccess = System.currentTimeMillis();
        accessCount.getAndIncrement();
        return obj;
    }

    /**
     * 获取过期时间，返回{@code null}表示永不过期
     *
     * @return 此对象的过期时间，返回{@code null}表示永不过期
     * @since 5.7.17
     */
    public LocalDateTime getExpiredTime() {
        if (!permanent) {
            return this.expireTime;
        }
        return null;
    }

    /**
     * 获取上次访问时间
     *
     * @return 上次访问时间
     * @since 5.7.17
     */
    public long getLastAccess() {
        return this.lastAccess;
    }

    public LocalDateTime getLastAccessTime() {
        return DateTimeUtils.millisToLocalTime(this.lastAccess);
    }

    /**
     * 是否过期。如果过期，外层需要删除此对象
     *
     * @return
     */
    public boolean isExpired() {
        if (!permanent) {
            return (System.currentTimeMillis() - this.expireTimeMillis) > 0;
        }
        return false;
    }

    /**
     * 刷新存储时间，在现有时间基础之上，增加初始化时的时长
     */
    public void refresh() {
        if (this.expire > 0) {
            this.permanent = false;
            this.expireTime = DateTimeUtils.toLocalDateTime(expire, this.unit);
            this.expireTimeMillis = DateTimeUtils.toMillis(this.expireTime);
        } else {
            this.permanent = true;
            this.expireTimeMillis = 0;
            this.expireTime = null;
        }
    }

    @Override
    public String toString() {
        return "CacheObj [key=" + key + ", obj=" + obj + ", lastAccess=" + lastAccess + ", accessCount=" + accessCount + "]";
    }
}

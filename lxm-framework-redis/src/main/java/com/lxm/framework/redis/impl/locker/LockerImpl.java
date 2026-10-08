package com.lxm.framework.redis.impl.locker;

import com.lxm.framework.redis.exceptions.RedisLockerOccupiedException;
import com.lxm.framework.redis.inf.LockerSession;
import com.lxm.framework.redis.parts.LockerCore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** 有到期时间的非重入锁；释放只能由获取锁的线程执行。任务必须在租约内完成。 */
public class LockerImpl implements LockerSession {
    private static final DefaultRedisScript<Long> RELEASE =
            new DefaultRedisScript<>(
                    "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
                    Long.class);
    private final StringRedisTemplate session;
    private final LockerCore core;
    private final ThreadLocal<String> owner = new ThreadLocal<>();

    public LockerImpl(StringRedisTemplate session, LockerCore core) {
        this.session = session;
        this.core = core;
    }

    private long leaseMillis() {
        long value = core.getExpireUnit().toMillis(core.getExpire());
        if (value <= 0) throw new IllegalArgumentException("Lock TTL must be positive");
        return value;
    }

    @Override
    public boolean tryLock() {
        if (owner.get() != null) return false;
        String token = UUID.randomUUID().toString();
        if (Boolean.TRUE.equals(
                session.opsForValue()
                        .setIfAbsent(
                                core.getFinKey(), token, leaseMillis(), TimeUnit.MILLISECONDS))) {
            owner.set(token);
            return true;
        }
        return false;
    }

    @Override
    public void releaseLock() {
        String token = owner.get();
        if (token == null) return;
        try {
            session.execute(RELEASE, List.of(core.getFinKey()), token);
        } finally {
            owner.remove();
        }
    }

    @Override
    public void accessResource(boolean hold) throws RedisLockerOccupiedException {
        long timeout = TimeUnit.MILLISECONDS.toNanos(leaseMillis());
        long start = System.nanoTime();
        long interval = core.getDurationUnit().toMillis(core.getDuration());
        if (interval <= 0)
            throw new IllegalArgumentException("Lock retry interval must be positive");
        while (!tryLock()) {
            if (!hold || System.nanoTime() - start >= timeout)
                throw new RedisLockerOccupiedException();
            try {
                TimeUnit.MILLISECONDS.sleep(
                        Math.min(
                                interval,
                                Math.max(
                                        1,
                                        TimeUnit.NANOSECONDS.toMillis(
                                                timeout - (System.nanoTime() - start)))));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                var failure = new RedisLockerOccupiedException();
                failure.initCause(ex);
                throw failure;
            }
        }
    }

    @Override
    public void accessResourceThenRelease(Runnable task) throws RedisLockerOccupiedException {
        accessResource();
        try {
            task.run();
        } finally {
            releaseLock();
        }
    }
}

package com.lxm.framework.redis;

import com.lxm.framework.redis.impl.locker.LockerImpl;
import com.lxm.framework.redis.parts.LockerCore;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class LockerIT {
    private LockerImpl lock(StringRedisTemplate template, String key, long ttl) {
        var core = new LockerCore();
        core.setKey(key);
        core.setExpire(ttl);
        core.setExpireUnit(TimeUnit.MILLISECONDS);
        core.setDuration(1L);
        core.setDurationUnit(TimeUnit.MILLISECONDS);
        return new LockerImpl(template, core);
    }

    @Test
    void expiredOwnerCannotDeleteNewLeaseAndFailureReleases() throws Exception {
        var config =
                new RedisStandaloneConfiguration(
                        "127.0.0.1", Integer.getInteger("lfp.redis.port", 16379));
        var first = new JedisConnectionFactory(config);
        var second = new JedisConnectionFactory(config);
        first.afterPropertiesSet();
        first.start();
        second.afterPropertiesSet();
        second.start();
        try {
            var one = new StringRedisTemplate(first);
            var two = new StringRedisTemplate(second);
            String key = "lfp-lock-test:" + java.util.UUID.randomUUID();
            var expired = lock(one, key, 40);
            var current = lock(two, key, 3000);
            assertTrue(expired.tryLock());
            Thread.sleep(80);
            assertTrue(current.tryLock());
            expired.releaseLock();
            assertFalse(lock(one, key, 3000).tryLock());
            current.releaseLock();
            var failing = lock(one, key, 3000);
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            failing.accessResourceThenRelease(
                                    () -> {
                                        throw new IllegalArgumentException();
                                    }));
            var next = lock(two, key, 3000);
            assertTrue(next.tryLock());
            next.releaseLock();
        } finally {
            first.destroy();
            second.destroy();
        }
    }
}

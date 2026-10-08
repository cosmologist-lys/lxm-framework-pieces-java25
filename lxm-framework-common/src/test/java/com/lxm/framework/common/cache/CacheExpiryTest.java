package com.lxm.framework.common.cache;

import com.lxm.framework.common.cache.customized.impl.CacheBuilder;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CacheExpiryTest {
    @Test
    void expiredValueCannotBeReturnedOrRefreshed() throws Exception {
        try (var cache = CacheBuilder.<String, String>newTimeCache(16, 1, TimeUnit.DAYS)) {
            var notifications = new AtomicInteger();
            cache.expireListener((key, value) -> notifications.incrementAndGet());
            cache.put("session", "old", 1, TimeUnit.MILLISECONDS);
            Thread.sleep(15);
            assertNull(cache.get(new String("session"), true));
            assertFalse(cache.exist("session"));
            assertEquals(0, cache.size());
            assertEquals(1, notifications.get());
            assertEquals(-1, cache.getTimeout("session"));
        }
    }

    @Test
    void permanentTimeoutAndEqualKeysAreConsistent() throws Exception {
        var cache = CacheBuilder.<String, String>newPermanentCache(16);
        cache.put("session", "old");
        assertEquals(0, cache.getTimeout("session"));
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var results = new java.util.ArrayList<Future<String>>();
            for (int i = 0; i < 8; i++)
                results.add(
                        executor.submit(
                                () -> {
                                    start.await();
                                    return cache.getThenRemove(new String("session"));
                                }));
            start.countDown();
            long winners = 0;
            for (var result : results) if (result.get() != null) winners++;
            assertEquals(1, winners);
            assertFalse(cache.exist("session"));
        }
    }
}

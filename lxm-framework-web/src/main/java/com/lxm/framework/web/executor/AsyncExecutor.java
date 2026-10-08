package com.lxm.framework.web.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @Author: Lys
 * @Date 2023/5/25
 * @Describe
 **/
@Slf4j
@Component
public class AsyncExecutor {

    private static SpringExecutor.LxmTaskExecutor taskExecutor;
    private static ExecutorService executorService;

    @Autowired
    public AsyncExecutor(
            @Qualifier("lxmExecutor") SpringExecutor.LxmTaskExecutor taskExecutor,
            @Qualifier("lxmExecutorService") ExecutorService executorService) {
        AsyncExecutor.taskExecutor = taskExecutor;
        AsyncExecutor.executorService = executorService;
        log.debug("lxm-async-executor init success");
    }

    /**
     * submit提交方式
     *
     * @param callable<T> callable
     * @return future
     */
    public static <T> Future<T> submit(Callable<T> callable) {
        return executorService.submit(callable);
    }

    /**
     * 循环执行，以防异步对表的操作的时候，写入事务尚未提交，但读取流程先到
     *
     * @param callable
     * @param <T>
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static <T> T submitAwait(Callable<T> callable) {
        try {
            Future<T> res = null;
            int interval = 1;
            boolean go = true;
            while (go) {
                res = executorService.submit(callable);
                if (Objects.nonNull(res.get()) || interval > 2000) {
                    go = false;
                } else {
                    interval += 400;
                }
            }
            return res.get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("async executor error : ", e);
        }
        return null;
    }

    /**
     * runnable提交方式，无返回值
     *
     * @param runnable 任务
     */
    public static void execute(Runnable runnable) {
        taskExecutor.execute(runnable);
    }
}

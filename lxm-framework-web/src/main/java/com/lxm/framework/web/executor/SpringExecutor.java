package com.lxm.framework.web.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.Serial;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: Lys
 * @Date 2023/5/25
 * @Describe
 **/
@Slf4j
@Configuration
public class SpringExecutor {

    @Bean("lxmExecutor")
    public LxmTaskExecutor taskExecutor() {
        var taskExecutor = new LxmTaskExecutor();
        //核心线程数
        taskExecutor.setCorePoolSize(200);
        //最大核心线程数
        taskExecutor.setMaxPoolSize(1000);
        //配置队列大小
        taskExecutor.setQueueCapacity(10000);
        //保持连接时间
        taskExecutor.setKeepAliveSeconds(30);
        // 队列限制
        taskExecutor.setQueueCapacity(4098);
        taskExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                log.debug("lxm-spring-executor , into reject execution");
                if (!executor.isShutdown()) {
                    r.run();
                } else {
                    log.error("lxm-executor error :");
                    executor.shutdown();
                }
            }
        });
        return taskExecutor;
    }

    @Bean("lxmExecutorService")
    public ExecutorService executorService() {
        // 64线程
        return Executors.newFixedThreadPool(64);
    }

    protected static final class LxmTaskExecutor extends ThreadPoolTaskExecutor implements AsyncUncaughtExceptionHandler {
        private static final long serialVersionUID = -6678654537325794545L;

        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.debug("lxm-async-task-executor catch error : {},method : {}", ex.getCause(), method.getName());
        }
    }
}

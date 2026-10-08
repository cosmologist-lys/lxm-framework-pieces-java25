package com.lxm.framework.redis.inf;

import com.lxm.framework.redis.exceptions.RedisLockerOccupiedException;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/2/7
 * @Describe
 **/
public interface LockerSession {

    /**
     * 锁
     *
     * @return
     */
    boolean tryLock();

    /**
     * 释放锁
     */
    void releaseLock();


    /**
     * 获取资源
     *
     * @param hold true=等待资源，false=不等待，若资源被占用，直接抛错
     * @return
     */
    void accessResource(boolean hold) throws RedisLockerOccupiedException;

    /**
     * 获取资源，默认方法，需要等待资源
     */
    default void accessResource() throws RedisLockerOccupiedException {
        accessResource(true);
    }

    /**
     * 获取资源之后执行runnable，然后释放资源
     *
     * @param anything
     */
    void accessResourceThenRelease(Runnable anything) throws RedisLockerOccupiedException;


}

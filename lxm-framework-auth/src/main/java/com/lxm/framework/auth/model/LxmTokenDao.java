package com.lxm.framework.auth.model;

import lombok.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/6/2
 * @Describe
 **/
public interface LxmTokenDao {

    // default time-unit = seconds
    TimeUnit SEC_UNIT = TimeUnit.SECONDS;

    // 以下所有的timeout时间单位都是s

    /**
     * 根据key获取value，如果没有，则返回空
     *
     * @param key 键名称
     * @return value
     */
    String get(@NonNull String key);

    /**
     * 写入指定key-value键值对，并设定过期时间 (单位: 秒)
     *
     * @param key     键名称
     * @param value   值
     * @param timeout 过期时间 (单位: 秒)
     */
    void set(@NonNull String key, @NonNull String value, long timeout);

    /**
     * 修改指定key的剩余存活时间 (单位: 秒)
     *
     * @param key     指定key
     * @param timeout 过期时间
     */
    void update(@NonNull String key, @NonNull String value, long timeout);

    /**
     * 修改指定key的剩余存活时间 (单位: 秒)
     *
     * @param key     指定key
     * @param timeout 过期时间
     */
    void updateTimeout(@NonNull String key, long timeout);

    /**
     * 删除一个指定的key
     *
     * @param key 键名称
     */
    void delete(@NonNull String key);

    /**
     * 获取指定key的剩余存活时间 (单位: 秒)
     *
     * @param key 指定key
     * @return 这个key的剩余存活时间
     */
    long getTimeout(@NonNull String key);

    //  for session //

    LxmSession getSession(@NonNull String key);

    /**
     * 保存session
     * @param session   /
     * @param timeout   /
     */
    void setSession(@NonNull LxmSession session, long timeout);

    /**
     * 更新session
     * @param session   /
     * @param timeout   /
     */
    void updateSession(@NonNull LxmSession session, long timeout);

    /**
     * 删除session
     * @param session   /
     */
    void deleteSession(@NonNull LxmSession session);

    /**
     * 保存已封禁的账号，注意这里不要直接用login-id为key，会和token的存储对象的key冲突。建议增加一个前缀为key。下面isForbid和permit方法同理
     * @param loginId
     * @param timeout
     */
    void setForbid(String loginId, long timeout);

    /**
     * 是否已被封禁
     * @param loginId
     * @return
     */
    boolean isForbid(String loginId);

    /**
     * 接触封禁
     * @param loginId
     */
    void permit(String loginId);
}

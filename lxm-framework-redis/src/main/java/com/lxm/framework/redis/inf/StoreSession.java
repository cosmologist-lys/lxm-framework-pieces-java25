package com.lxm.framework.redis.inf;

import lombok.NonNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/2/6
 * @Describe  存储类redis-session
 **/
public interface StoreSession extends Serializable {

    boolean exist();

    long expire();

    void store(@NonNull Object value);

    void store(@NonNull List<?> list);

    LocalDateTime expireAt();

    String getKey();

    <T> T get(Class<T> tClass);

    <T> T get();

    <T> T getThenDelete(Class<T> tClass);

    List<?> getList();

    List<?> getListThenDelete();
}

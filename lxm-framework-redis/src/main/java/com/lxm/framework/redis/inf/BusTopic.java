package com.lxm.framework.redis.inf;

import lombok.NonNull;

/**
 * @Author: Lys
 * @Date 2023/5/12
 * @Describe
 **/
public interface BusTopic<T>{

    T topic(@NonNull String topic);

    T topics(@NonNull String... topics);
}

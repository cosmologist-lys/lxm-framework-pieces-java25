package com.lxm.framework.common.cache.customized.inf;

import java.io.Serializable;

/**
 * @Author: Lys
 * @Date 2023/1/17
 * @Describe
 **/
public interface Immutable<T> extends Serializable {

    T get();

}

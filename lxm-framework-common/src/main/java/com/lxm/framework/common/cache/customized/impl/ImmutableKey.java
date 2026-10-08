package com.lxm.framework.common.cache.customized.impl;

import com.lxm.framework.common.cache.customized.inf.Immutable;

/**
 * @Author: Lys
 * @Date 2023/1/17
 * @Describe  不可变包装类。用作cache里的key的封装层。
 **/
public class ImmutableKey<T> implements Immutable<T> {

    private static final long serialVersionUID = 5032216555312041416L;

    private final T key;

    public static <T> ImmutableKey<T> of(T t) {
        return new ImmutableKey<>(t);
    }

    private ImmutableKey(T key) {
        //this.uniq =  (uniq instanceof String) ? (String) uniq : String.valueOf(uniq.hashCode());
        this.key = key;
    }

    @Override
    public T get() {
        return this.key;
    }

    /**
     * 以下重写hashCode和equals方法。目的是存储为hashmap时，作为key，避免相同的内容找不到
     *
     * @return
     */
    @Override
    public int hashCode() {
        return null == key ? 0 : key.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() == obj.getClass()) {
            final ImmutableKey<?> that = (ImmutableKey<?>) obj;
            return this.key.equals(that.key);
        }
        return false;
    }
}

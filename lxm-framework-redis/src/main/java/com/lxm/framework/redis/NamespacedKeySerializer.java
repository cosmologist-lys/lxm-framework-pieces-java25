package com.lxm.framework.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/** 新版 JSON 不解释旧版任意类型标记；通过独立命名空间隔离存量值。 */
public final class NamespacedKeySerializer implements RedisSerializer<String> {
    private final String namespace;
    public NamespacedKeySerializer(String namespace) {
        if (namespace == null || namespace.isBlank()) throw new IllegalArgumentException("Redis namespace is required");
        this.namespace = namespace;
    }
    public byte[] serialize(String value) { return value == null ? null : StringRedisSerializer.UTF_8.serialize(namespace + value); }
    public String deserialize(byte[] value) {
        String key = StringRedisSerializer.UTF_8.deserialize(value);
        if (key == null) return null;
        if (!key.startsWith(namespace)) throw new IllegalArgumentException("Foreign Redis namespace");
        return key.substring(namespace.length());
    }
}

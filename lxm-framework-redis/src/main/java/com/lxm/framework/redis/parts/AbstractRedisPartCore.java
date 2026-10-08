package com.lxm.framework.redis.parts;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/5/8
 * @Describe
 **/
public abstract class AbstractRedisPartCore implements Serializable {

    private static final long serialVersionUID = -5408920301256551077L;

    private String prefix;
    private String key;
    private String finKey;
    private Long expire;

    public Long getExpire() {
        return expire;
    }

    public AbstractRedisPartCore setExpire(Long expire) {
        this.expire = expire;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public AbstractRedisPartCore setPrefix(String prefix) {
        if (StringUtils.isNotBlank(prefix)){
            this.prefix = prefix.trim();
        }
        return this;
    }

    public String getKey() {
        return key;
    }

    public AbstractRedisPartCore setKey(String key) {
        if (StringUtils.isNotBlank(key)){
            this.key = key.trim();
        }
        return this;
    }

    public String getFinKey() {
        if (null != finKey && finKey.length() > 0){
            return finKey;
        }
        String k = "";
        if (null != prefix){
            k = prefix.concat("-");
        }
        finKey = k.concat(key);
        return finKey;
    }

    public AbstractRedisPartCore setFinKey(String finKey) {
        if (StringUtils.isNotBlank(finKey)){
            this.finKey = finKey;
        }
        return this;
    }
}

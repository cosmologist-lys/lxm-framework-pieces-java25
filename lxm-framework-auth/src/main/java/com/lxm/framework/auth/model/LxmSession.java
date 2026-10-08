package com.lxm.framework.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Lys
 * @Date 2023/5/30
 * @Describe
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class LxmSession implements Serializable {
    private static final long serialVersionUID = -8201967752906487560L;

    /**
     * 此Session的id
     */
    @Getter @Setter private String loginId;

    /**
     * 此Session的创建时间
     */
    @Getter @Setter private long createTimestamp;

    /**
     * 此Session的所有挂载数据
     */
    @Setter private Map<String, Object> dataMap = new ConcurrentHashMap<>();

    @Setter private List<TokenBox> tokenList = new Vector<>();

    public LxmSession() {
        this(null);
    }

    public LxmSession(String loginId) {
        this.loginId = loginId;
        this.createTimestamp = System.currentTimeMillis();
    }

    public void addTokenBox(TokenBox tokenBox) {
        for (TokenBox one : tokenList) {
            if (one.getTokenValue().equals(tokenBox.getTokenValue())) {
                return;
            }
        }
        tokenList.add(tokenBox);
    }

    public TokenBox getTokenBox(String tokenValue) {
        for (TokenBox one : tokenList) {
            if (one.getTokenValue().equals(tokenValue)) {
                return one;
            }
        }
        return null;
    }

    /**
     * 返回token签名列表的拷贝副本
     *
     * @return token签名列表
     */
    public List<TokenBox> getTokenList() {
        return new Vector<>(tokenList);
    }

    public void removeTokenBox(String tokenValue) {
        final TokenBox tokenBox = getTokenBox(tokenValue);
        if (null != tokenBox) {
            tokenList.remove(tokenBox);
        }
    }

    /**
     * 写入一个值
     *
     * @param key   名称
     * @param value 值
     */
    public void setAttribute(String key, Object value) {
        dataMap.put(key, value);
        update();
    }

    /**
     * 取出一个值
     *
     * @param key 名称
     * @return 值
     */
    public Object getAttribute(String key) {
        return dataMap.get(key);
    }

    /**
     * 取值，并指定取不到值时的默认值
     *
     * @param key          名称
     * @param defaultValue 取不到值的时候返回的默认值
     * @return value
     */
    public Object getAttribute(String key, Object defaultValue) {
        Object value = getAttribute(key);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    /**
     * 移除一个值
     *
     * @param key 要移除的值的名字
     */
    public void removeAttribute(String key) {
        dataMap.remove(key);
        update();
    }

    /**
     * 清空所有值
     */
    public void clearAttribute() {
        dataMap.clear();
        update();
    }

    /**
     * 是否含有指定key
     *
     * @param key 是否含有指定值
     * @return 是否含有
     */
    public boolean containsAttribute(String key) {
        return dataMap.containsKey(key);
    }

    /**
     * 返回当前session会话所有key
     *
     * @return 所有值的key列表
     */
    public Set<String> attributeKeys() {
        return dataMap.keySet();
    }

    /**
     * 获取数据挂载集合（如果更新map里的值，请调用session.update()方法避免产生脏数据 ）
     *
     * @return 返回底层储存值的map对象
     */
    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    /**
     * 写入数据集合 (不改变底层对象，只将此dataMap所有数据进行替换)
     *
     * @param dataMap 数据集合
     */
    public void refreshDataMap(Map<String, Object> dataMap) {
        this.dataMap.clear();
        this.dataMap.putAll(dataMap);
        this.update();
    }

    /**
     * 更新Session（从持久库更新刷新一下）
     */
    public void update() {
        // AuthManager.getSaTokenDao().updateSession(this);
    }

    public boolean isEmpty() {
        return tokenList.size() == 0;
    }
}

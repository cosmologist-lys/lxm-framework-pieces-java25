package com.lxm.framework.web.jsonresult.filter;

import java.io.Serializable;
import java.util.*;

/** 每个 JsonResult 的过滤规则；通过单次序列化上下文传递，不修改共享 Mapper。 */
public class JsonResultProvider implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<Class<?>, Set<String>> includeMap = new HashMap<>();
    private final Map<Class<?>, Set<String>> excludeMap = new HashMap<>();
    public Map<Class<?>, Set<String>> getIncludeMap() { return Collections.unmodifiableMap(includeMap); }
    public Map<Class<?>, Set<String>> getExcludeMap() { return Collections.unmodifiableMap(excludeMap); }
    public void include(Class<?> type, String[] fields) { includeMap.computeIfAbsent(type, k -> new HashSet<>()).addAll(Arrays.asList(fields)); }
    public void exclude(Class<?> type, String[] fields) { excludeMap.computeIfAbsent(type, k -> new HashSet<>()).addAll(Arrays.asList(fields)); }
    public boolean allows(Class<?> type, String field) {
        if (includeMap.containsKey(type)) return includeMap.get(type).contains(field);
        return !excludeMap.getOrDefault(type, Set.of()).contains(field);
    }
}

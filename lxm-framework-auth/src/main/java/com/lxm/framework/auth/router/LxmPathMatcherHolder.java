package com.lxm.framework.auth.router;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * @Author: Lys
 * @Date 2023/5/30
 * @Describe
 **/
public class LxmPathMatcherHolder {

    private static PathMatcher pathMatcher;

    /**
     * 获取路由匹配器
     * @return 路由匹配器
     */
    public static PathMatcher getPathMatcher() {
        if(pathMatcher == null) {
            pathMatcher = new AntPathMatcher();
        }
        return pathMatcher;
    }

    /**
     * 写入路由匹配器
     * @param pathMatcher 路由匹配器
     */
    public static void setPathMatcher(PathMatcher pathMatcher) {
        LxmPathMatcherHolder.pathMatcher = pathMatcher;
    }

}

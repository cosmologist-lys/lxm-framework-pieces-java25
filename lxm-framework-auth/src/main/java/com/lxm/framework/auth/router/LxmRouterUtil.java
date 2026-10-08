package com.lxm.framework.auth.router;

import com.lxm.framework.auth.filter.LxmFunction;
import com.lxm.framework.auth.model.AuthManager;

import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/5/30
 * @Describe
 **/
public class LxmRouterUtil {

    /**
     * 使用路由匹配符集合与当前URI执行匹配 (并指定排除匹配符)，如果匹配成功则执行验证函数
     *
     * @param patterns        路由匹配符集合
     * @param excludePatterns 要排除的路由匹配符集合
     * @param function        要执行的方法
     */
    public static void match(
            List<String> patterns, List<String> excludePatterns, LxmFunction function) {
        if (isMatchCurrURI(patterns)) {
            if (!isMatchCurrURI(excludePatterns)) {
                function.run();
            }
        }
    }

    /**
     * 校验指定路由匹配符是否可以匹配成功当前URI
     *
     * @param patterns 路由匹配符
     * @return 是否匹配成功
     */
    public static boolean isMatchCurrURI(List<String> patterns) {
        return isMatch(patterns, AuthManager.getContext().getRequest().getRequestURI());
    }

    /**
     * 校验指定路由匹配符是否可以匹配成功指定路径
     *
     * @param patterns 路由匹配符
     * @param path     需要匹配的路径集合
     * @return 是否匹配成功
     */
    public static boolean isMatch(List<String> patterns, String path) {
        for (String pattern : patterns) {
            if (isMatch(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验指定路由匹配符是否可以匹配成功指定路径
     *
     * @param pattern 路由匹配符
     * @param path    需要匹配的路径
     * @return 是否匹配成功
     */
    public static boolean isMatch(String pattern, String path) {
        return AuthManager.getContext().matchPath(pattern, path);
    }
}

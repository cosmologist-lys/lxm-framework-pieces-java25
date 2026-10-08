package com.lxm.framework.auth.filter;

import com.lxm.framework.auth.model.AuthFilterRegister;
import com.lxm.framework.auth.router.LxmRouterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/5/30
 * @Describe
 **/
@Slf4j
public class LxmServletFilter implements Filter {

    /**
     * 拦截路由
     */
    private List<String> includeList = new ArrayList<>();

    /**
     * 放行路由
     */
    private List<String> excludeList = new ArrayList<>();

    private ServletFilterStrategy auth;

    private ServletErrorStrategy error;

    @Autowired(required = false)
    public LxmServletFilter(AuthFilterRegister filterConfig) {
        this.setIncludeList(filterConfig.includes());
        this.setExcludeList(filterConfig.excludes());
        this.setAuth(filterConfig.whenBlocked());
        this.setError(filterConfig.whenError());
        log.debug("lxm-auth servlet filter initialized success");
    }

    /**
     * 添加 [拦截路由]
     *
     * @param paths 路由
     * @return 对象自身
     */
    public LxmServletFilter addInclude(String... paths) {
        includeList.addAll(Arrays.asList(paths));
        return this;
    }

    /**
     * 添加 [放行路由]
     *
     * @param paths 路由
     * @return 对象自身
     */
    public LxmServletFilter addExclude(String... paths) {
        excludeList.addAll(Arrays.asList(paths));
        return this;
    }

    /**
     * 写入 [拦截路由] 集合
     *
     * @param pathList 路由集合
     * @return 对象自身
     */
    public LxmServletFilter setIncludeList(List<String> pathList) {
        includeList = new ArrayList<>(pathList);
        return this;
    }

    /**
     * 写入 [放行路由] 集合
     *
     * @param pathList 路由集合
     * @return 对象自身
     */
    public LxmServletFilter setExcludeList(List<String> pathList) {
        excludeList = new ArrayList<>(pathList);
        return this;
    }

    /**
     * 获取 [拦截路由] 集合
     *
     * @return see note
     */
    public List<String> getIncludeList() {
        return includeList;
    }

    /**
     * 获取 [放行路由] 集合
     *
     * @return see note
     */
    public List<String> getExcludeList() {
        return excludeList;
    }

    public LxmServletFilter setAuth(ServletFilterStrategy strategy) {
        this.auth = strategy;
        return this;
    }

    public LxmServletFilter setError(ServletErrorStrategy error) {
        this.error = error;
        return this;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            // 执行全局过滤器
            LxmRouterUtil.match(includeList, excludeList, () -> auth.run());
        } catch (Exception failure) {
            var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
            response.setContentType("application/json;charset=UTF-8");
            if (response instanceof jakarta.servlet.http.HttpServletResponse http) {
                http.setStatus(
                        failure
                                        instanceof
                                        com.lxm.framework.common.auth.errs.AbstractAuthException
                                                authFailure
                                ? (authFailure.getCode() == 1002 ? 403 : 401)
                                : 500);
                http.setHeader("Cache-Control", "no-store");
            }
            // ServletErrorStrategy 明确返回 JSON 字符串，先解析校验，再写出实际 JSON。
            var body = mapper.readTree(error.run(failure));
            if (body == null || !body.isObject())
                throw new ServletException("Auth error strategy must return a JSON object");
            response.getWriter().write(mapper.writeValueAsString(body));
            return;
        }
        // 执行
        chain.doFilter(request, response);
    }
}

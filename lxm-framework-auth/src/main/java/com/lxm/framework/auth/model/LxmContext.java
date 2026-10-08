package com.lxm.framework.auth.model;

/**
 * @Author: Lys
 * @Date 2023/5/29
 * @Describe
 **/
public interface LxmContext {

    LxmRequest getRequest();

    LxmResponse getResponse();

    boolean matchPath(String pattern, String path);
}

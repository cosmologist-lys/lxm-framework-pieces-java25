package com.lxm.framework.common.web;

import java.io.Serializable;

/**
 * @Author: Lys
 * @Date 2023/1/10
 * @Describe
 **/
public interface JsonResultInterface<T> extends Serializable {

    int getCode();

    String getMessage();

    T getData();

    String getSign();
}

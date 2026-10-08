package com.lxm.framework.common.principle;

/** 由应用提供可信的当前身份；不能从未认证的请求参数构造。 */
@FunctionalInterface
public interface PrincipleProvider {
    StandardPrinciple current();
}

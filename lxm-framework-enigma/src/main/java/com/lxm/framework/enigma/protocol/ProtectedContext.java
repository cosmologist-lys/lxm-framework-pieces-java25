package com.lxm.framework.enigma.protocol;

import com.lxm.framework.enigma.spi.EnigmaIdentity;

/** 一个请求固定使用认证时的密钥快照；轮换不改变在途响应使用的 kid。 */
public final class ProtectedContext {
    public final RequestContext request;
    public final KeySnapshot key;
    public final EnigmaIdentity identity;
    public boolean authenticated;
    public boolean admitted;

    public ProtectedContext(RequestContext request, KeySnapshot key, EnigmaIdentity identity) {
        this.request = request;
        this.key = key;
        this.identity = identity;
    }
}

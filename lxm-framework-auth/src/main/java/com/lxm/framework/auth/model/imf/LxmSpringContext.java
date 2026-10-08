package com.lxm.framework.auth.model.imf;

import com.lxm.framework.auth.model.LxmContext;
import com.lxm.framework.auth.model.LxmRequest;
import com.lxm.framework.auth.model.LxmResponse;
import com.lxm.framework.auth.router.LxmPathMatcherHolder;
import com.lxm.framework.auth.utils.SpringMVCUtil;

/**
 * @Author: Lys
 * @Date 2023/5/29
 * @Describe
 **/
public class LxmSpringContext implements LxmContext {

    @Override
    public LxmRequest getRequest() {
        return new LxmSpringRequest(SpringMVCUtil.getRequest());
    }

    @Override
    public LxmResponse getResponse() {
        return new LxmSpringResponse(SpringMVCUtil.getResponse());
    }

    @Override
    public boolean matchPath(String pattern, String path) {
        return LxmPathMatcherHolder.getPathMatcher().match(pattern, path);
    }
}

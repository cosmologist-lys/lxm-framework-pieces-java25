package com.lxm.framework.auth.model;

import com.lxm.framework.auth.filter.PermissionDefaultBlankFilter;
import com.lxm.framework.auth.filter.PrincipleDefaultFilter;
import com.lxm.framework.auth.filter.PrincipleFilter;
import com.lxm.framework.auth.filter.RoleDefaultBlankFilter;
import com.lxm.framework.auth.model.defaults.LxmDefaultTokenListener;
import com.lxm.framework.auth.model.defaults.TokenDaoCacheDefault;
import com.lxm.framework.auth.model.imf.LxmSpringContext;

/**
 * @Author: Lys
 * @Date 2023/5/30
 * @Describe
 **/
public class AuthManager {

    // just a locker
    private static final Object LOCKER = new Object();

    private static volatile LxmContext context;

    public static void setContext(LxmContext context) {
        AuthManager.context = context;
    }

    public static LxmContext getContext() {
        if (null == context) {
            synchronized (LOCKER) {
                if (null == context) {
                    setContext(new LxmSpringContext());
                }
            }
        }
        return context;
    }

    private static volatile LxmTokenListener listener;

    public static void setListener(LxmTokenListener listener) {
        AuthManager.listener = listener;
    }

    public static LxmTokenListener getListener() {
        if (null == listener) {
            synchronized (LOCKER) {
                if (null == listener) {
                    setListener(new LxmDefaultTokenListener());
                }
            }
        }
        return listener;
    }

    private static volatile LxmTokenProperties properties;

    public static void setProperties(LxmTokenProperties properties) {
        AuthManager.properties = properties;
    }

    public static LxmTokenProperties getProperties() {
        if (null == properties) {
            synchronized (LOCKER) {
                if (null == properties) {
                    setProperties(new LxmTokenProperties());
                }
            }
        }
        return properties;
    }

    private static volatile LxmTokenDao tokenDao;

    public static void setTokenDao(LxmTokenDao tokenDao) {
        AuthManager.tokenDao = tokenDao;
    }

    public static LxmTokenDao getTokenDao() {
        if (null == tokenDao) {
            synchronized (LOCKER) {
                if (null == tokenDao) {
                    setTokenDao(new TokenDaoCacheDefault());
                }
            }
        }
        return tokenDao;
    }

    private static volatile PrincipleFilter principleFilter;

    public static void setPrincipleFilter(PrincipleFilter principleFilter) {
        AuthManager.principleFilter = principleFilter;
    }

    public static PrincipleFilter getPrincipleFilter() {
        if (null == principleFilter) {
            synchronized (LOCKER) {
                if (null == principleFilter) {
                    setPrincipleFilter(new PrincipleDefaultFilter(new RoleDefaultBlankFilter(), new PermissionDefaultBlankFilter()));
                }
            }
        }
        return principleFilter;
    }
}

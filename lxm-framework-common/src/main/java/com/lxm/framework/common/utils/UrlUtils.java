package com.lxm.framework.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @Author: Lys
 * @Date 2023/1/10
 * @Describe
 **/
public class UrlUtils {

    private static final String PROTOCOL = "http";

    private static final String DASH = "://";

    public static String getBaseUrl(String url) {
        if (!StringUtils.startsWith(url, PROTOCOL)) {
            return null;
        }
        try {
            URL u = new URL(url);
            String protocol = u.getProtocol();
            String host = u.getHost();
            return protocol.concat(DASH).concat(host);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUri(String url) {
        if (!StringUtils.startsWith(url, PROTOCOL)) {
            return url;
        }
        String baseUrl = getBaseUrl(url);
        return StringUtils.replace(url, baseUrl, "");
    }
}

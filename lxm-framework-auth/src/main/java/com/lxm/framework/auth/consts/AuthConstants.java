package com.lxm.framework.auth.consts;

/**
 * @Author: Lys
 * @Date 2023/6/2
 * @Describe
 **/
public class AuthConstants {

    public static final Integer MAX_INT = Integer.MAX_VALUE;

    public static final String TOURIST = "lxm-tourist-";

    public static final String DEFAULT_DEVICE = "default-device";

    public static final String TOKEN_TAG = "lxm-auth-token-";

    public static final String TOKEN_FORBID_TAG = "lxm-forbid-lid-";

    /**
     * token风格: uuid
     */
    public static final String TOKEN_STYLE_UUID = "uuid";

    /**
     * token风格: 简单uuid (不带下划线)
     */
    public static final String TOKEN_STYLE_SIMPLE_UUID = "simple-uuid";

    /**
     * token风格: 32位随机字符串
     */
    public static final String TOKEN_STYLE_RANDOM_32 = "random-32";

    /**
     * token风格: 64位随机字符串
     */
    public static final String TOKEN_STYLE_RANDOM_64 = "random-64";

    /**
     * token风格: 128位随机字符串
     */
    public static final String TOKEN_STYLE_RANDOM_128 = "random-128";

    /**
     * token风格: tik风格 (2_14_16)
     */
    public static final String TOKEN_STYLE_TIK = "tik";
}

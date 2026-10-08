package com.lxm.framework.common.utils;

import cn.hutool.crypto.digest.DigestUtil;
import com.lxm.framework.common.AppException;
import org.apache.commons.lang3.StringUtils;
import lombok.NonNull;

import java.nio.charset.StandardCharsets;

/**
 * @Author: Lys
 * @Date 2023/1/10
 * @Describe
 **/
public class CryptoUtils {

    public static String sha256Hex(String txt) {
        return DigestUtil.sha256Hex(txt, String.valueOf(StandardCharsets.UTF_8));
    }

    public static String md5Hex(String txt) {
        return DigestUtil.md5Hex(txt, String.valueOf(StandardCharsets.UTF_8));
    }

    /**
     * @param txt       原文
     * @param salt      盐
     * @param flipCount 翻转次数
     * @return
     */
    public static String md5HexWithSaltAndFlip(@NonNull String txt, String salt, int flipCount) {
        String cipher = txt;
        if (StringUtils.isNotBlank(salt)) {
            cipher = txt.concat(salt);
        }
        if (flipCount < 0) {
            throw new AppException(-1, "flip-count需要大于0");
        }
        for (int i = 0; i < flipCount; i++) {
            cipher = md5Hex(cipher);
        }
        return cipher;
    }

}

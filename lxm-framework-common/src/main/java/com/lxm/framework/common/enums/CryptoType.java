package com.lxm.framework.common.enums;

import java.util.Arrays;

/**
 * @Author: Lys
 * @Date 2023/1/9
 * @Describe
 **/
public enum CryptoType {
    MD5("md5"),
    SHA1("sha1"),
    SHA256("sha256"),
    BASE64("base64"),
    AES("aes");

    public String getTypeString() {
        return typeString;
    }

    private final String typeString;

    CryptoType(String typeString) {
        this.typeString = typeString;
    }

    public static CryptoType mapping(String type) {
        return Arrays.stream(values())
                .filter(one -> one.getTypeString().equalsIgnoreCase(type))
                .findFirst()
                .orElse(MD5);
    }
}

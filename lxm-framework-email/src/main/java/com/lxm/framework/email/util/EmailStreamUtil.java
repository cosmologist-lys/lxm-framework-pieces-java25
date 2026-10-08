package com.lxm.framework.email.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @Author: Lys
 * @Date 2023/1/10
 * @Describe
 **/
public class EmailStreamUtil {

    public static String copyStreamToString(InputStream in, Charset charset) throws IOException {
        if (null == in) {
            return "";
        }
        StringBuilder out = new StringBuilder(4096);
        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[4096];

        int charsRead;
        while ((charsRead = reader.read(buffer)) != -1) {
            out.append(buffer, 0, charsRead);
        }
        return out.toString();
    }
}

package com.lxm.framework.web.util;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Author: Lys
 * @Date 2023/1/10
 * @Describe
 **/
public class StreamToFileUtils {

    public static void toFile(
            HttpServletResponse response,
            String content,
            Charset charset,
            String fileName,
            String extension) {
        toFile(response, content.getBytes(charset), fileName, extension);
    }

    public static void toFile(HttpServletResponse response, String content, String fileName) {
        toFile(response, content.getBytes(StandardCharsets.UTF_8), fileName);
    }

    public static void toFile(HttpServletResponse response, byte[] bytes, String fileName) {
        toFile(response, bytes, fileName, null);
    }

    public static void toFile(
            HttpServletResponse response, byte[] bytes, String fileName, String extension) {
        if (StringUtils.isBlank(extension)) {
            extension = "txt";
        }
        final String fullFileName = fileName.concat(".").concat(extension);
        response.setHeader(
                "Content-disposition",
                "attachment;filename=" + URLEncoder.encode(fullFileName, StandardCharsets.UTF_8));
        response.setContentType("application/x-download");
        try (ServletOutputStream os = response.getOutputStream()) {
            os.write(bytes);
            os.flush();
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}

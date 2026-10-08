package com.lxm.framework.enigma.protocol;

import com.lxm.framework.enigma.EnigmaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.util.*;

public record RequestContext(
        int v,
        String mode,
        String direction,
        String sid,
        String kid,
        String method,
        String path,
        Map<String, List<String>> params,
        String contentType,
        String timestamp,
        String nonce) {
    public static final List<String> HEADERS =
            List.of(
                    "X-Enigma-Version",
                    "X-Enigma-Mode",
                    "X-Enigma-Session",
                    "X-Enigma-Kid",
                    "X-Enigma-Timestamp",
                    "X-Enigma-Nonce");

    public static String header(HttpServletRequest request, String name) {
        var values = Collections.list(request.getHeaders(name));
        if (values.size() != 1 || values.getFirst().length() > 256)
            throw EnigmaException.protocol();
        return values.getFirst();
    }

    public static RequestContext from(
            HttpServletRequest request, String expectedMode, String prefix) {
        if (!"1".equals(header(request, "X-Enigma-Version"))
                || !expectedMode.equals(header(request, "X-Enigma-Mode")))
            throw EnigmaException.protocol();
        String sid = header(request, "X-Enigma-Session"),
                kid = header(request, "X-Enigma-Kid"),
                nonce = header(request, "X-Enigma-Nonce");
        if (!sid.matches("[A-Za-z0-9_-]{22}") || !kid.matches("[A-Za-z0-9_-]{22}"))
            throw EnigmaException.protocol();
        com.lxm.framework.enigma.crypto.EnigmaCrypto.decode(nonce, 16);
        String timestamp = header(request, "X-Enigma-Timestamp");
        if (!timestamp.matches("[1-9][0-9]{0,15}")) throw EnigmaException.protocol();
        String type = "";
        if (request.getContentType() != null && !request.getContentType().isBlank()) {
            try {
                var media = MediaType.parseMediaType(request.getContentType());
                if (!MediaType.APPLICATION_JSON.isCompatibleWith(media)
                        || (media.getCharset() != null
                                && !StandardCharsets.UTF_8.equals(media.getCharset())))
                    throw EnigmaException.protocol();
                type = "application/json";
            } catch (IllegalArgumentException invalid) {
                throw EnigmaException.protocol();
            }
        }
        if ("ENCRYPT".equals(expectedMode) && !"application/json".equals(type))
            throw EnigmaException.protocol();
        if (request.getHeader("Content-Encoding") != null
                && !"identity".equalsIgnoreCase(request.getHeader("Content-Encoding")))
            throw EnigmaException.protocol();
        String path = prefix + request.getRequestURI();
        if (path.length() > 4096) throw EnigmaException.protocol();
        return new RequestContext(
                1,
                expectedMode,
                "request",
                sid,
                kid,
                request.getMethod().toUpperCase(Locale.ROOT),
                path,
                query(request.getQueryString()),
                type,
                timestamp,
                nonce);
    }

    public static Map<String, List<String>> query(String raw) {
        if (raw == null || raw.isEmpty()) return Map.of();
        if (raw.length() > 8192) throw EnigmaException.protocol();
        var params = new LinkedHashMap<String, List<String>>();
        int values = 0;
        for (String pair : raw.split("&", -1)) {
            if (pair.isEmpty()) continue;
            int equals = pair.indexOf('=');
            String name = decode(equals < 0 ? pair : pair.substring(0, equals));
            String value = decode(equals < 0 ? "" : pair.substring(equals + 1));
            params.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            if (++values > 256 || params.size() > 128) throw EnigmaException.protocol();
        }
        var immutable = new LinkedHashMap<String, List<String>>();
        params.forEach((key, list) -> immutable.put(key, List.copyOf(list)));
        return Collections.unmodifiableMap(immutable);
    }

    private static String decode(String raw) {
        var bytes = new ByteArrayOutputStream();
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == '%') {
                if (i + 2 >= raw.length()) throw EnigmaException.protocol();
                int high = Character.digit(raw.charAt(++i), 16),
                        low = Character.digit(raw.charAt(++i), 16);
                if (high < 0 || low < 0) throw EnigmaException.protocol();
                bytes.write(high * 16 + low);
            } else if (ch == '+') bytes.write(' ');
            else {
                if (ch > 127) throw EnigmaException.protocol();
                bytes.write(ch);
            }
        }
        return ProtocolJson.utf8(bytes.toByteArray());
    }
}

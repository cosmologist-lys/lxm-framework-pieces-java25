package org.example;

import com.lxm.framework.enigma.crypto.EnigmaCrypto;
import com.lxm.framework.enigma.protocol.*;
import org.erdtman.jcs.JsonCanonicalizer;
import java.nio.file.*;
import java.util.*;

/** 离线固定向量生成器；所有公开密钥均为测试常量。 */
public final class GenerateEnigmaVectors {
    private static byte[] bytes(int offset, int length) {
        byte[] value = new byte[length];
        for (int i = 0; i < length; i++) value[i] = (byte) (offset + i);
        return value;
    }

    public static void main(String[] args) throws Exception {
        var crypto = new EnigmaCrypto();
        String sid = EnigmaCrypto.encode(bytes(16, 16)),
                kid = EnigmaCrypto.encode(bytes(32, 16)),
                nonce = EnigmaCrypto.encode(bytes(48, 16));
        var sign =
                new RequestContext(
                        1,
                        "SIGN",
                        "request",
                        sid,
                        kid,
                        "PUT",
                        "/demo/params",
                        Map.of("q", List.of("中文", "+", ""), "bare", List.of("")),
                        "",
                        "1800000000000",
                        nonce);
        var encrypt =
                new RequestContext(
                        1,
                        "ENCRYPT",
                        "request",
                        sid,
                        kid,
                        "POST",
                        "/demo/body",
                        Map.of(),
                        "application/json",
                        "1800000000000",
                        nonce);
        Object canonicalInput =
                Map.of(
                        "10",
                        1,
                        "2",
                        2,
                        "numbers",
                        List.of(333333333.33333329, 1e30, 4.50, 2e-3, 1e-27),
                        "unicode",
                        "中文💡",
                        "literals",
                        Arrays.asList(null, true, false));
        Object plaintext =
                Map.of(
                        "name",
                        "中文",
                        "secret",
                        "private",
                        "id",
                        "9007199254740993",
                        "numbers",
                        List.of(4.50, 2e-3, 1e-27));
        var output = new LinkedHashMap<String, Object>();
        output.put("canonicalInput", canonicalInput);
        output.put(
                "canonicalExpected",
                new JsonCanonicalizer(ProtocolJson.MAPPER.writeValueAsString(canonicalInput))
                        .getEncodedString());
        output.put("signContext", sign);
        output.put(
                "signature",
                EnigmaCrypto.encode(crypto.sign(bytes(64, 32), ProtocolJson.canonical(sign))));
        output.put("requestSigning", EnigmaCrypto.encode(bytes(64, 32)));
        output.put("requestEncryption", EnigmaCrypto.encode(bytes(0, 32)));
        output.put("encryptContext", encrypt);
        output.put("plaintext", plaintext);
        output.put("iv", EnigmaCrypto.encode(bytes(0, 12)));
        output.put(
                "ciphertext",
                EnigmaCrypto.encode(
                        crypto.encrypt(
                                bytes(0, 32),
                                bytes(0, 12),
                                ProtocolJson.canonical(encrypt),
                                ProtocolJson.canonical(plaintext))));
        Path path = Path.of(args[0]);
        Files.createDirectories(path.getParent());
        Files.writeString(
                path,
                ProtocolJson.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(output)
                        + "\n");
    }
}

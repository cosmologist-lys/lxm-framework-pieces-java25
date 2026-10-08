package com.lxm.framework.enigma.protocol;

import com.lxm.framework.enigma.*;
import com.lxm.framework.enigma.crypto.EnigmaCrypto;
import tools.jackson.databind.JsonNode;
import java.time.Clock;
import java.util.*;

public final class EnigmaProtocol {
    private final EnigmaCrypto crypto;
    private final EnigmaSessionService sessions;
    private final EnigmaProperties properties;
    private final Clock clock;

    public EnigmaProtocol(
            EnigmaCrypto crypto,
            EnigmaSessionService sessions,
            EnigmaProperties properties,
            Clock clock) {
        this.crypto = crypto;
        this.sessions = sessions;
        this.properties = properties;
        this.clock = clock;
    }

    public void verifySign(ProtectedContext context, String signature) {
        crypto.verify(
                EnigmaCrypto.decode(context.key.requestSigning(), 32),
                ProtocolJson.canonical(context.request),
                signature);
        context.authenticated = true;
        sessions.admit(context, null);
    }

    public byte[] decrypt(ProtectedContext context, byte[] body) {
        if (body.length > properties.getMaxEnvelopeBytes()) throw EnigmaException.protocol();
        var value = ProtocolJson.read(body);
        if (!value.isObject()
                || value.size() != 4
                || !value.path("v").isIntegralNumber()
                || value.path("v").asInt() != 1
                || !value.path("kid").isString()
                || !context.request.kid().equals(value.path("kid").asString())
                || !value.path("iv").isString()
                || !value.path("ciphertext").isString()) throw EnigmaException.protocol();
        String iv = value.path("iv").asString();
        byte[] cipher = EnigmaCrypto.decode(value.path("ciphertext").asString(), -1);
        if (cipher.length < 16 || cipher.length > properties.getMaxBodyBytes() + 16)
            throw EnigmaException.protocol();
        byte[] plaintext =
                crypto.decrypt(
                        EnigmaCrypto.decode(context.key.requestEncryption(), 32),
                        EnigmaCrypto.decode(iv, 12),
                        ProtocolJson.canonical(context.request),
                        cipher);
        context.authenticated = true;
        sessions.admit(context, iv);
        ProtocolJson.read(plaintext);
        return plaintext;
    }

    public record Response(byte[] body, Map<String, String> headers) {}

    public Response protect(ProtectedContext context, int status, byte[] serialized) {
        if (status < 200 || status == 204 || status == 205 || status == 304)
            throw EnigmaException.protocol();
        if (serialized.length > properties.getMaxBodyBytes()) throw EnigmaException.protocol();
        var value = ProtocolJson.read(serialized);
        if (!value.isObject()
                || !value.path("code").isIntegralNumber()
                || !value.path("code").canConvertToInt()
                || !value.path("message").isString()
                || !value.has("data")) throw EnigmaException.protocol();
        String timestamp = Long.toString(clock.millis()), nonce = crypto.randomId();
        JsonNode data = value.get("data");
        String mode =
                "ENCRYPT".equals(context.request.mode()) && !data.isNull() ? "ENCRYPT" : "SIGN";
        var authentication = new LinkedHashMap<String, Object>();
        authentication.put("v", 1);
        authentication.put("request", context.request);
        authentication.put("direction", "response");
        authentication.put("timestamp", timestamp);
        authentication.put("nonce", nonce);
        authentication.put("status", status);
        authentication.put("code", value.path("code").asInt());
        authentication.put("message", value.path("message").asString());
        authentication.put("protection", mode);
        authentication.put("unexecuted", !context.admitted);
        var output = ProtocolJson.MAPPER.createObjectNode();
        output.put("code", value.path("code").asInt());
        output.put("message", value.path("message").asString());
        if ("ENCRYPT".equals(mode)) {
            String iv = EnigmaCrypto.encode(crypto.randomBytes(12));
            sessions.reserveResponseIv(context, iv);
            byte[] ciphertext =
                    crypto.encrypt(
                            EnigmaCrypto.decode(context.key.responseEncryption(), 32),
                            EnigmaCrypto.decode(iv, 12),
                            ProtocolJson.canonical(authentication),
                            ProtocolJson.canonical(data));
            output.set(
                    "data",
                    ProtocolJson.MAPPER.valueToTree(
                            Map.of(
                                    "v",
                                    1,
                                    "kid",
                                    context.request.kid(),
                                    "iv",
                                    iv,
                                    "ciphertext",
                                    EnigmaCrypto.encode(ciphertext))));
        } else {
            authentication.put("data", data);
            output.set("data", data);
            output.put(
                    "sign",
                    EnigmaCrypto.encode(
                            crypto.sign(
                                    EnigmaCrypto.decode(context.key.responseSigning(), 32),
                                    ProtocolJson.canonical(authentication))));
        }
        byte[] wire = ProtocolJson.canonical(output);
        if (wire.length > properties.getMaxEnvelopeBytes()) throw EnigmaException.protocol();
        return new Response(
                wire,
                Map.of(
                        "X-Enigma-Version",
                        "1",
                        "X-Enigma-Session",
                        context.request.sid(),
                        "X-Enigma-Kid",
                        context.request.kid(),
                        "X-Enigma-Timestamp",
                        timestamp,
                        "X-Enigma-Nonce",
                        nonce,
                        "X-Enigma-Mode",
                        mode,
                        "X-Enigma-Unexecuted",
                        Boolean.toString(!context.admitted)));
    }
}

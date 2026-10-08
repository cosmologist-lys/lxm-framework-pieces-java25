package com.lxm.framework.enigma.store;

import com.lxm.framework.enigma.protocol.ProtocolJson;
import java.util.Map;
import java.nio.charset.StandardCharsets;

final class EnigmaCryptoOwner {
    private EnigmaCryptoOwner() {}

    static String binding(Map<String, String> owner) {
        return new String(ProtocolJson.canonical(owner), StandardCharsets.UTF_8);
    }
}

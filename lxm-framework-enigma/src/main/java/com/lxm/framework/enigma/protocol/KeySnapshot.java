package com.lxm.framework.enigma.protocol;

import com.lxm.framework.enigma.store.SessionState.KeyVersion;

public record KeySnapshot(
        String kid,
        String requestEncryption,
        String responseEncryption,
        String requestSigning,
        String responseSigning,
        long createdAt,
        long expiresAt) {
    public static KeySnapshot of(KeyVersion key) {
        return new KeySnapshot(
                key.kid,
                key.requestEncryption,
                key.responseEncryption,
                key.requestSigning,
                key.responseSigning,
                key.createdAt,
                key.expiresAt);
    }

    @Override
    public String toString() {
        return "KeySnapshot[kid=" + kid + "]";
    }
}

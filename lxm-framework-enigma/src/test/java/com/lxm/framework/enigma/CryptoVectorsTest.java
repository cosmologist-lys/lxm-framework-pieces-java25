package com.lxm.framework.enigma;

import com.lxm.framework.enigma.crypto.EnigmaCrypto;
import com.lxm.framework.enigma.protocol.ProtocolJson;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.HexFormat;
import static org.junit.jupiter.api.Assertions.*;

class CryptoVectorsTest {
    @Test
    void hkdfMatchesRfc5869CaseOneFirst32Bytes() {
        var crypto = new EnigmaCrypto();
        var hex = HexFormat.of();
        byte[] ikm = new byte[22];
        Arrays.fill(ikm, (byte) 0x0b);
        assertEquals(
                "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf",
                hex.formatHex(
                        crypto.hkdf(
                                ikm,
                                hex.parseHex("000102030405060708090a0b0c"),
                                hex.parseHex("f0f1f2f3f4f5f6f7f8f9"))));
    }

    @Test
    void matchesCommittedCrossLanguageFixtures() throws Exception {
        try (var input = getClass().getResourceAsStream("/enigma-vectors.json")) {
            assertNotNull(input);
            var fixture = ProtocolJson.MAPPER.readTree(input);
            var crypto = new EnigmaCrypto();
            assertEquals(
                    fixture.path("canonicalExpected").asString(),
                    new org.erdtman.jcs.JsonCanonicalizer(
                                    ProtocolJson.MAPPER.writeValueAsString(
                                            fixture.path("canonicalInput")))
                            .getEncodedString());
            assertEquals(
                    fixture.path("signature").asString(),
                    EnigmaCrypto.encode(
                            crypto.sign(
                                    EnigmaCrypto.decode(
                                            fixture.path("requestSigning").asString(), 32),
                                    ProtocolJson.canonical(fixture.path("signContext")))));
            assertEquals(
                    fixture.path("ciphertext").asString(),
                    EnigmaCrypto.encode(
                            crypto.encrypt(
                                    EnigmaCrypto.decode(
                                            fixture.path("requestEncryption").asString(), 32),
                                    EnigmaCrypto.decode(fixture.path("iv").asString(), 12),
                                    ProtocolJson.canonical(fixture.path("encryptContext")),
                                    ProtocolJson.canonical(fixture.path("plaintext")))));
        }
    }
}

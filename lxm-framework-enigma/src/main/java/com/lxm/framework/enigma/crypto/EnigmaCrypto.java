package com.lxm.framework.enigma.crypto;

import com.lxm.framework.enigma.EnigmaException;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;

public final class EnigmaCrypto {
    private final SecureRandom random;

    public EnigmaCrypto() {
        this(new SecureRandom());
    }

    public EnigmaCrypto(SecureRandom random) {
        this.random = random;
    }

    public byte[] randomBytes(int size) {
        byte[] value = new byte[size];
        random.nextBytes(value);
        return value;
    }

    public String randomId() {
        return encode(randomBytes(16));
    }

    public static String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    public static byte[] decode(String value, int expectedSize) {
        if (value == null || value.isEmpty() || !value.matches("[A-Za-z0-9_-]+"))
            throw EnigmaException.protocol();
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(value);
            if (!encode(decoded).equals(value)
                    || (expectedSize >= 0 && decoded.length != expectedSize))
                throw EnigmaException.protocol();
            return decoded;
        } catch (IllegalArgumentException failure) {
            throw EnigmaException.protocol();
        }
    }

    public byte[] encrypt(byte[] key, byte[] iv, byte[] aad, byte[] plaintext) {
        return crypt(Cipher.ENCRYPT_MODE, key, iv, aad, plaintext);
    }

    public byte[] decrypt(byte[] key, byte[] iv, byte[] aad, byte[] ciphertext) {
        return crypt(Cipher.DECRYPT_MODE, key, iv, aad, ciphertext);
    }

    private byte[] crypt(int mode, byte[] key, byte[] iv, byte[] aad, byte[] input) {
        if (key.length != 32 || iv.length != 12) throw EnigmaException.protocol();
        try {
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(mode, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            cipher.updateAAD(aad);
            return cipher.doFinal(input);
        } catch (AEADBadTagException invalid) {
            throw EnigmaException.integrity();
        } catch (GeneralSecurityException unavailable) {
            throw new IllegalStateException("Required JCA algorithm unavailable", unavailable);
        }
    }

    public byte[] sign(byte[] key, byte[] message) {
        if (key.length != 32) throw EnigmaException.protocol();
        return hmac(key, message);
    }

    private byte[] hmac(byte[] key, byte[] message) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(message);
        } catch (GeneralSecurityException unavailable) {
            throw new IllegalStateException("Required JCA algorithm unavailable", unavailable);
        }
    }

    /** RFC 5869 SHA-256，固定输出 32 字节，只需要一个 Expand block。 */
    public byte[] hkdf(byte[] inputKey, byte[] salt, byte[] info) {
        byte[] pseudoRandomKey = hmac(salt.length == 0 ? new byte[32] : salt, inputKey);
        byte[] block = java.util.Arrays.copyOf(info, info.length + 1);
        block[info.length] = 1;
        return hmac(pseudoRandomKey, block);
    }

    public void verify(byte[] key, byte[] message, String signature) {
        if (!MessageDigest.isEqual(sign(key, message), decode(signature, 32)))
            throw EnigmaException.integrity();
    }
}

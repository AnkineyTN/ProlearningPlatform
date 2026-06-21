package com.cabybara.prolearningplatform.utils;

import com.cabybara.prolearningplatform.exception.EncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM symmetric encryption for secrets stored at rest (user LLM API keys).
 * <p>
 * Output format: {@code Base64( IV(12 bytes) || ciphertext || GCM auth tag(16 bytes) )}.
 * The master key is supplied via {@code llm-encryption.master-key} (Base64 of exactly 32 bytes).
 * <p>
 * If the master key is absent the encryptor starts in a disabled state and any encrypt/decrypt call
 * throws — this keeps the application context loadable in environments where BYOK is not configured
 * (e.g. local dev / tests) while still failing fast on an explicitly malformed key.
 */
@Component
@Slf4j
public class AesGcmEncryptor {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;        // recommended GCM nonce size
    private static final int GCM_TAG_LENGTH_BITS = 128;   // auth tag length
    private static final int AES_KEY_LENGTH_BYTES = 32;   // AES-256

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKey masterKey;

    public AesGcmEncryptor(@Value("${llm-encryption.master-key:}") String masterKeyBase64) {
        if (masterKeyBase64 == null || masterKeyBase64.isBlank()) {
            this.masterKey = null;
            log.warn("LLM_MASTER_KEY is not configured — LLM API key encryption is DISABLED until it is set.");
            return;
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(masterKeyBase64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("LLM_MASTER_KEY is not valid Base64", e);
        }
        if (keyBytes.length != AES_KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "LLM_MASTER_KEY must decode to exactly 32 bytes (AES-256); got " + keyBytes.length);
        }
        this.masterKey = new SecretKeySpec(keyBytes, "AES");
        log.info("AesGcmEncryptor initialized (AES-256-GCM).");
    }

    /** Encrypts {@code plaintext} and returns Base64( IV || ciphertext || tag ). */
    public String encrypt(String plaintext) {
        requireConfigured();
        if (plaintext == null) {
            throw new EncryptionException("Cannot encrypt null value");
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            // Never log plaintext / key material.
            throw new EncryptionException("Failed to encrypt value", e);
        }
    }

    /** Decrypts a Base64( IV || ciphertext || tag ) string produced by {@link #encrypt(String)}. */
    public String decrypt(String stored) {
        requireConfigured();
        if (stored == null || stored.isBlank()) {
            throw new EncryptionException("Cannot decrypt empty value");
        }
        try {
            byte[] combined = Base64.getDecoder().decode(stored);
            if (combined.length <= IV_LENGTH_BYTES) {
                throw new EncryptionException("Stored ciphertext is too short or corrupt");
            }
            byte[] iv = new byte[IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);
            byte[] ciphertext = new byte[combined.length - IV_LENGTH_BYTES];
            System.arraycopy(combined, IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (EncryptionException e) {
            throw e;
        } catch (Exception e) {
            // AEADBadTagException (tamper / wrong key) and any other failure land here.
            throw new EncryptionException("Failed to decrypt value", e);
        }
    }

    private void requireConfigured() {
        if (masterKey == null) {
            throw new EncryptionException("LLM master key is not configured");
        }
    }
}

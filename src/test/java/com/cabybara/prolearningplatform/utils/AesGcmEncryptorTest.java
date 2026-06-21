package com.cabybara.prolearningplatform.utils;

import com.cabybara.prolearningplatform.exception.EncryptionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AesGcmEncryptorTest {

    // Deterministic Base64-encoded 32-byte key for tests.
    private static final String KEY = "Phe6lq4EzTuZhp5W8K2/Om+3TSVVF13MbosJFFeasDY=";

    @Test
    void encryptThenDecryptRoundTrips() {
        AesGcmEncryptor encryptor = new AesGcmEncryptor(KEY);
        String plaintext = "sk-super-secret-api-key-12345";

        String encrypted = encryptor.encrypt(plaintext);

        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);
        assertEquals(plaintext, encryptor.decrypt(encrypted));
    }

    @Test
    void encryptUsesRandomIvSoCiphertextsDiffer() {
        AesGcmEncryptor encryptor = new AesGcmEncryptor(KEY);
        String plaintext = "same-input";

        assertNotEquals(encryptor.encrypt(plaintext), encryptor.encrypt(plaintext));
    }

    @Test
    void decryptTamperedCiphertextThrows() {
        AesGcmEncryptor encryptor = new AesGcmEncryptor(KEY);
        String encrypted = encryptor.encrypt("hello");

        // Flip the last character to corrupt the ciphertext / auth tag.
        char last = encrypted.charAt(encrypted.length() - 1);
        String tampered = encrypted.substring(0, encrypted.length() - 1) + (last == 'A' ? 'B' : 'A');

        assertThrows(EncryptionException.class, () -> encryptor.decrypt(tampered));
    }

    @Test
    void decryptWithDifferentKeyThrows() {
        String encrypted = new AesGcmEncryptor(KEY).encrypt("hello");
        AesGcmEncryptor other = new AesGcmEncryptor("S2ME9pVi4Wj/QZnqH7IBsGGxOA08qP93gmYqBCnpMhQ=");

        assertThrows(EncryptionException.class, () -> other.decrypt(encrypted));
    }

    @Test
    void notConfiguredEncryptorThrowsOnUse() {
        AesGcmEncryptor encryptor = new AesGcmEncryptor("");

        assertThrows(EncryptionException.class, () -> encryptor.encrypt("x"));
        assertThrows(EncryptionException.class, () -> encryptor.decrypt("x"));
    }

    @Test
    void invalidKeyLengthFailsFast() {
        assertThrows(IllegalStateException.class, () -> new AesGcmEncryptor("dG9vLXNob3J0"));
    }
}

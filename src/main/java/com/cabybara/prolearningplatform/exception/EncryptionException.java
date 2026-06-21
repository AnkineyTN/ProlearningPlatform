package com.cabybara.prolearningplatform.exception;

/**
 * Wraps any failure while encrypting/decrypting secured credentials (e.g. user LLM API keys).
 * Mapped to HTTP 500 — never carries plaintext, key material, or ciphertext in its message.
 */
public class EncryptionException extends RuntimeException {
    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

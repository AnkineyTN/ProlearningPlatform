package com.cabybara.prolearningplatform.dto.internal;

import com.cabybara.prolearningplatform.enums.LlmProvider;

/**
 * In-memory holder for a user's decrypted LLM config, used only when forwarding a request to the
 * AI Service. Never JSON-serialized to clients. {@link #toString()} masks the API key so it cannot
 * leak through accidental logging.
 */
public record DecryptedLlmConfig(LlmProvider provider, String model, String apiKey) {

    @Override
    public String toString() {
        return "DecryptedLlmConfig{provider=" + provider + ", model=" + model + ", apiKey=***}";
    }
}

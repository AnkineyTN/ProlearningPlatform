package com.cabybara.prolearningplatform.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Supported LLM providers for the Bring-Your-Own-Key (BYOK) feature.
 * The {@code wireValue} (lowercase) is what gets sent to the AI Service in the {@code X-LLM-Provider} header.
 */
public enum LlmProvider {
    OPENAI("openai"),
    ANTHROPIC("anthropic"),
    GOOGLE("google"),
    GROQ("groq");

    private final String wireValue;

    LlmProvider(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String getWireValue() {
        return wireValue;
    }

    @JsonCreator
    public static LlmProvider fromWireValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Unsupported LLM provider: null. Allowed: openai, anthropic, google, groq");
        }
        return Arrays.stream(values())
                .filter(p -> p.wireValue.equalsIgnoreCase(value) || p.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported LLM provider: " + value + ". Allowed: openai, anthropic, google, groq"));
    }
}

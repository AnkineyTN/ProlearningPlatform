package com.cabybara.prolearningplatform.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Body sent to the AI Service's {@code POST /llm/test-connection}. The AI Service expects
 * snake_case {@code api_key}, unlike this app's own camelCase convention.
 */
public record AiTestConnectionRequest(
        String provider,
        String model,
        @JsonProperty("api_key") String apiKey) {

    @Override
    public String toString() {
        return "AiTestConnectionRequest{provider=" + provider + ", model=" + model + ", apiKey=***}";
    }
}

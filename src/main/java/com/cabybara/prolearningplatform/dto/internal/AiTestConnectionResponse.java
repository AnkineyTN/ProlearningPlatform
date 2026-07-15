package com.cabybara.prolearningplatform.dto.internal;

/** Raw envelope returned by the AI Service's {@code POST /llm/test-connection}. */
public record AiTestConnectionResponse(AiTestConnectionResult data) {
}

package com.cabybara.prolearningplatform.dto.internal;

/** The {@code data} object inside the AI Service's test-connection response. */
public record AiTestConnectionResult(boolean valid, String provider, String model, String message) {
}

package com.cabybara.prolearningplatform.exception;

/**
 * Thrown when an AI generation feature is invoked but the current user has no active LLM
 * configuration (provider + model + API key). Mapped to HTTP 400 so the frontend can prompt
 * the user to configure their AI provider in settings.
 */
public class LlmNotConfiguredException extends RuntimeException {
    public LlmNotConfiguredException() {
        super("No active LLM configuration found. Please configure your AI provider, model, and API key in settings.");
    }

    public LlmNotConfiguredException(String message) {
        super(message);
    }
}

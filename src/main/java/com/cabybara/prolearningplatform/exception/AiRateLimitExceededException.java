package com.cabybara.prolearningplatform.exception;

/**
 * Thrown when the user exceeds the configured rate limit (quota) for AI APIs.
 * Mapped to HTTP 429 Too Many Requests in GlobalExceptionHandler.
 */
public class AiRateLimitExceededException extends RuntimeException {
    public AiRateLimitExceededException() {
        super("You have exceeded your AI API usage limit. Please try again later.");
    }

    public AiRateLimitExceededException(String message) {
        super(message);
    }
}

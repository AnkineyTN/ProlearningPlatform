package com.cabybara.prolearningplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Represents a failure returned by the external AI Service (or the upstream LLM provider) that
 * should be surfaced to the frontend with a friendly message. Carries the HTTP status to return.
 * The raw upstream body / secrets are never propagated through this exception.
 */
@Getter
public class AIServiceException extends RuntimeException {

    private final HttpStatus status;

    public AIServiceException(String message) {
        this(HttpStatus.BAD_GATEWAY, message);
    }

    public AIServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public AIServiceException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}

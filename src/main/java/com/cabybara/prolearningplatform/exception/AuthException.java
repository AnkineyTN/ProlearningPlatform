package com.cabybara.prolearningplatform.exception;

import lombok.Data;
import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
public class AuthException extends RuntimeException {
    HttpStatus status;

    public AuthException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}

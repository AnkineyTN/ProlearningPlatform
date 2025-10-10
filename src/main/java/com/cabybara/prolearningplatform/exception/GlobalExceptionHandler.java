package com.cabybara.prolearningplatform.exception;

import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(
            AuthException.class
    )
    public ResponseEntity<ApiResponse<Object>> handleAuthException(AuthException ex) {
        ApiResponse<Object> response = ResponseUtil.error("Auth failed: " + ex.getMessage(), null);
        return ResponseEntity
                .status(ex.getStatus())
                .body(response);
    }

    @ExceptionHandler(
            GoogleAuthException.class
    )
    public ResponseEntity<ApiResponse<Object>> handleGoogleAuthException(GoogleAuthException ex) {
        ApiResponse<Object> response = ResponseUtil.error("Google auth failed: " + ex.getMessage(), null);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }
}

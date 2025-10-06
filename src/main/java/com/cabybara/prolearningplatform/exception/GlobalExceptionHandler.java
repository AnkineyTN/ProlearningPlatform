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
        ApiResponse<Object> response = ResponseUtil.error(ex.getMessage(), null);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}

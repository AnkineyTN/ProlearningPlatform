package com.cabybara.prolearningplatform.exception;

import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(
            {
                    MethodArgumentNotValidException.class,
                    ConstraintViolationException.class,
                    HttpMessageNotReadableException.class
            })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date(System.currentTimeMillis()));
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

        String message = e.getMessage();
        if (e instanceof MethodArgumentNotValidException) {
            int start = message.lastIndexOf("[");
            int end = message.lastIndexOf("]");
            message = message.substring(start + 1, end - 1);
            errorResponse.setError("Payload invalid");
        } else if (e instanceof ConstraintViolationException) {
            message = message.substring(message.indexOf(" ") + 1);
            errorResponse.setError("Parameter invalid");
        } else if (e instanceof HttpMessageNotReadableException) {
            message = "Incorrect value enum. Correct enum values are: " + message.substring(message.indexOf("Enum class:") + 12).trim();
            errorResponse.setError("Incorrect enum value");
        }

        errorResponse.setMessage(message);
        return errorResponse;
    }

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

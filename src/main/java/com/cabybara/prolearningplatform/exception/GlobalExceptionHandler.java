package com.cabybara.prolearningplatform.exception;

import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ---- 400 Bad Request: validation & business logic ----

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request body", request);
    }

    @ExceptionHandler({
            BadRequestException.class,
            FlashcardStudySessionException.class,
            InvalidSortFieldException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(
            RuntimeException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // ---- 401 Unauthorized ----

    @ExceptionHandler({AuthException.class, GoogleAuthException.class})
    public ResponseEntity<ApiResponse<Object>> handleAuthentication(
            RuntimeException ex, WebRequest request) {
        HttpStatus status = (ex instanceof AuthException authEx)
                ? authEx.getStatus()
                : HttpStatus.UNAUTHORIZED;
        return buildResponse(status, ex.getMessage(), request);
    }

    // ---- 403 Forbidden ----

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // ---- 404 Not Found ----

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // ---- 409 Conflict ----

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflict(
            ResourceAlreadyExistsException ex, WebRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ---- 500 Internal Server Error ----

    @ExceptionHandler(UploadFileException.class)
    public ResponseEntity<ApiResponse<Object>> handleUploadFile(
            UploadFileException ex, WebRequest request) {
        log.error("File upload failed: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(
            IllegalStateException ex, WebRequest request) {
        log.error("Illegal state: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred", request);
    }

    @ExceptionHandler(OtpException.class)
    public ResponseEntity<?> handleOtpException(OtpException ex, WebRequest request) {
        log.error("OTP error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

   @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailNotVerified(EmailNotVerifiedException ex, WebRequest request) {
        log.warn("Email not verified | {}", ex.getMessage());
        String path = request.getDescription(false).replace("uri=", "");
        ApiResponse<Object> body = ResponseUtil.error(
                ex.getMessage(), null, "EMAIL_NOT_VERIFIED", path
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // ---- Helper ----

    private ResponseEntity<ApiResponse<Object>> buildResponse(
            HttpStatus status, String message, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ApiResponse<Object> body = ResponseUtil.error(message, null, "path: " + path);
        return ResponseEntity.status(status).body(body);
    }
}

package com.cabybara.prolearningplatform.exception;

import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void handlesMethodArgumentNotValid() throws Exception {
        mockMvc.perform(post("/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message", containsString("name: must not be blank")))
                .andExpect(jsonPath("$.metadata").value("path: /validation"));
    }

    @Test
    void handlesMalformedRequestBody() throws Exception {
        mockMvc.perform(post("/malformed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"))
                .andExpect(jsonPath("$.metadata").value("path: /malformed"));
    }

    @Test
    void handlesBadRequestExceptions() throws Exception {
        mockMvc.perform(get("/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("bad request"));

        mockMvc.perform(get("/flashcard-bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("flashcard issue"));

        mockMvc.perform(get("/invalid-sort"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid sort field: title"));
    }

    @Test
    void handlesUnauthorizedExceptions() throws Exception {
        mockMvc.perform(get("/auth-error"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("unauthorized"));

        mockMvc.perform(get("/google-auth-error"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("google auth failed"));
    }

    @Test
    void handlesAccessDenied() throws Exception {
        mockMvc.perform(get("/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("forbidden"));
    }

    @Test
    void handlesNotFoundAndConflict() throws Exception {
        mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("missing"));

        mockMvc.perform(get("/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("exists"));
    }

    @Test
    void handlesInternalServerErrors() throws Exception {
        mockMvc.perform(get("/upload-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("upload failed"));

        mockMvc.perform(get("/illegal-state"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("illegal state"));

        mockMvc.perform(get("/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void handlesOtpEmailVerificationAndBlockedAccount() throws Exception {
        mockMvc.perform(get("/otp-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("otp failed"));

        mockMvc.perform(get("/email-not-verified"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Email has not been verified."))
                .andExpect(jsonPath("$.metadata.code").value("EMAIL_NOT_VERIFIED"))
                .andExpect(jsonPath("$.metadata.path").value("/email-not-verified"));

        mockMvc.perform(get("/account-blocked"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Your account has been suspended. Please submit an appeal to request reinstatement."))
                .andExpect(jsonPath("$.metadata.code").value("ACCOUNT_BLOCKED"))
                .andExpect(jsonPath("$.metadata.path").value("/account-blocked"));
    }

    @RestController
    static class TestController {
        @PostMapping("/validation")
        String validation(@Valid @RequestBody TestRequest request) {
            return "ok";
        }

        @PostMapping("/malformed")
        String malformed(@RequestBody TestRequest request) {
            return "ok";
        }

        @GetMapping("/bad-request")
        String badRequest() {
            throw new BadRequestException("bad request");
        }

        @GetMapping("/flashcard-bad-request")
        String flashcardBadRequest() {
            throw new FlashcardStudySessionException("flashcard issue");
        }

        @GetMapping("/invalid-sort")
        String invalidSort() {
            throw new InvalidSortFieldException("title");
        }

        @GetMapping("/auth-error")
        String authError() {
            throw new AuthException(org.springframework.http.HttpStatus.UNAUTHORIZED, "unauthorized");
        }

        @GetMapping("/google-auth-error")
        String googleAuthError() {
            throw new GoogleAuthException("google auth failed");
        }

        @GetMapping("/forbidden")
        String forbidden() {
            throw new AccessDeniedException("forbidden");
        }

        @GetMapping("/not-found")
        String notFound() {
            throw new ResourceNotFoundException("missing");
        }

        @GetMapping("/conflict")
        String conflict() {
            throw new ResourceAlreadyExistsException("exists");
        }

        @GetMapping("/upload-error")
        String uploadError() {
            throw new UploadFileException("upload failed");
        }

        @GetMapping("/illegal-state")
        String illegalState() {
            throw new IllegalStateException("illegal state");
        }

        @GetMapping("/unexpected")
        String unexpected() {
            throw new RuntimeException("boom");
        }

        @GetMapping("/otp-error")
        String otpError() {
            throw new OtpException("otp failed");
        }

        @GetMapping("/email-not-verified")
        String emailNotVerified() {
            throw new EmailNotVerifiedException();
        }

        @GetMapping("/account-blocked")
        String accountBlocked() {
            throw new AccountBlockedException("blocked");
        }
    }

    record TestRequest(@NotBlank String name) {
    }
}

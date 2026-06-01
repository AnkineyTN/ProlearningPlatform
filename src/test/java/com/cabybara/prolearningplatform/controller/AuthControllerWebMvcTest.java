package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.user.ForgotPasswordRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.LoginRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.LogoutRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.RefreshTokenRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.response.user.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.RefreshTokenResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.RegisterResponseDto;
import com.cabybara.prolearningplatform.exception.AccountBlockedException;
import com.cabybara.prolearningplatform.exception.AuthException;
import com.cabybara.prolearningplatform.service.auth.AuthService;
import com.cabybara.prolearningplatform.service.auth.GoogleAuthService;
import com.cabybara.prolearningplatform.support.WebMvcTestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(WebMvcTestSecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private GoogleAuthService googleAuthService;

    private RegisterRequestDto validRegisterRequest() {
        return new RegisterRequestDto("John", "Doe", "secret123", "john@example.com", "USER");
    }

    @Test
    void registerReturns201WithSuccessEnvelope() throws Exception {
        RegisterResponseDto created = RegisterResponseDto.builder()
                .id(1L).firstName("John").lastName("Doe").email("john@example.com").build();
        when(authService.registerUser(any(RegisterRequestDto.class))).thenReturn(created);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Registration successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void registerRejectsInvalidEmailWith400() throws Exception {
        RegisterRequestDto invalid = new RegisterRequestDto("John", "Doe", "secret123", "not-an-email", "USER");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));

        verifyNoInteractions(authService);
    }

    @Test
    void loginReturns200WithTokens() throws Exception {
        LoginResponseDto response = LoginResponseDto.builder()
                .accessToken("access-token").refreshToken("refresh-token").build();
        when(authService.authenticateAndGenerateToken("john@example.com", "secret12"))
                .thenReturn(response);
        LoginRequestDto request = new LoginRequestDto("secret12", "john@example.com");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    void loginWithBadCredentialsReturns401() throws Exception {
        when(authService.authenticateAndGenerateToken(anyString(), anyString()))
                .thenThrow(new AuthException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        LoginRequestDto request = new LoginRequestDto("wrongpass", "john@example.com");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void loginWithBlockedAccountReturns403WithCode() throws Exception {
        when(authService.authenticateAndGenerateToken(anyString(), anyString()))
                .thenThrow(new AccountBlockedException("blocked"));
        LoginRequestDto request = new LoginRequestDto("secret12", "john@example.com");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.metadata.code").value("ACCOUNT_BLOCKED"));
    }

    @Test
    void loginRejectsShortPasswordWith400() throws Exception {
        LoginRequestDto request = new LoginRequestDto("short", "john@example.com");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));

        verifyNoInteractions(authService);
    }

    @Test
    void forgotPasswordReturns200() throws Exception {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("john@example.com");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(authService).forgotPassword("john@example.com");
    }

    @Test
    void refreshRejectsBlankTokenWith400() throws Exception {
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder().refreshToken("").build();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));

        verifyNoInteractions(authService);
    }

    @Test
    void refreshReturns200WithRotatedTokens() throws Exception {
        RefreshTokenResponseDto rotated = RefreshTokenResponseDto.builder()
                .accessToken("new-access").refreshToken("new-refresh").build();
        when(authService.refresh("old-refresh")).thenReturn(rotated);
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder().refreshToken("old-refresh").build();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh"));
    }

    @Test
    void logoutWithoutAuthenticationReturns401() throws Exception {
        LogoutRequestDto request = LogoutRequestDto.builder().refreshToken("refresh-token").build();

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService);
    }

    @Test
    void logoutWithJwtReturns200AndRevokes() throws Exception {
        LogoutRequestDto request = LogoutRequestDto.builder().refreshToken("refresh-token").build();

        mockMvc.perform(post("/auth/logout")
                        .with(jwt().jwt(jwt -> jwt.claim("id", 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Logged out"));

        verify(authService).logout(any(), anyLong(), anyString());
    }
}

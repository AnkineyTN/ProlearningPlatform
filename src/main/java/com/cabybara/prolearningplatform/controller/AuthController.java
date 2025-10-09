package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.*;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.service.AuthService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final GoogleAuthorizationCodeFlow googleFlow;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> registerNormalUser(@Valid @RequestBody RegisterRequestDto registerRequestDto) throws Exception {
        RegisterResponseDto registerResponseDto = authService.registerUser(registerRequestDto);
        ApiResponse<RegisterResponseDto> response = ResponseUtil.success("Registration successfully", registerResponseDto, null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> registerAdminUser(@Valid @RequestBody RegisterRequestDto registerRequestDto) throws Exception {
        RegisterResponseDto registerResponseDto = authService.registerAdmin(registerRequestDto);
        ApiResponse<RegisterResponseDto> response = ResponseUtil.success("Registration successfully", registerResponseDto, null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto loginResponseDto = authService.authenticateAndGenerateToken(
                loginRequestDto.getEmail(),
                loginRequestDto.getPassword()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Login successfully", loginResponseDto, null));

    }

    @GetMapping("/google/login")
    public ResponseEntity<ApiResponse<Object>> loginWithGoogle() throws IOException {
        Object responseData = authService.loginWithGoogle();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Authorization Code", responseData, null));
    }

    @PostMapping("/google/login/mb")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginWithGoogleMobile(@RequestBody String tokenId) throws GeneralSecurityException, IOException {
        LoginResponseDto loginResponseDto = authService.loginWithGoogleMobile(tokenId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", loginResponseDto, null));
    }

    @GetMapping("/google/callback")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletResponse response) throws Exception {
        authService.googleAuthCallback(code, state, error, response);
    }
}

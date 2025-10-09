package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.*;
import com.cabybara.prolearningplatform.service.AuthService;
import com.cabybara.prolearningplatform.service.GoogleAuthService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
@Validated
public class AuthController {
    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @Operation(
        summary = "Register normal user",
        description = "Registers a new user with the provided registration details."
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> registerNormalUser(@RequestBody @Valid RegisterRequestDto registerRequestDto) throws Exception {
        RegisterResponseDto registerResponseDto = authService.registerUser(registerRequestDto);
        ApiResponse<RegisterResponseDto> response = ResponseUtil.success("Registration successfully", registerResponseDto, null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Hidden
    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> registerAdminUser(@RequestBody @Valid RegisterRequestDto registerRequestDto) throws Exception {
        RegisterResponseDto registerResponseDto = authService.registerAdmin(registerRequestDto);
        ApiResponse<RegisterResponseDto> response = ResponseUtil.success("Registration successfully", registerResponseDto, null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "Login user",
            description = "User login with email and password"
    )
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

    @Operation(
            summary = "Login with user for web application",
            description = "Web application login with google process:\n" +
                    "- Call this api to get an auththorization url (AUTH_URL)\n" +
                    "- Redirect user to AUTH_URL"
    )
    @GetMapping("/google/login")
    public ResponseEntity<ApiResponse<GoogleAuthUrlResponseDto>> loginWithGoogle() throws IOException {
        GoogleAuthUrlResponseDto responseData = googleAuthService.loginWithGoogle();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Authorization Code", responseData, null));
    }

    @Operation(
            summary = "Login with user for mobile application",
            description = "Mobile application call this api to get access token"
    )
    @PostMapping("/google/login/mb")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginWithGoogleMobile(@RequestBody String tokenId) throws GeneralSecurityException, IOException {
        LoginResponseDto loginResponseDto = googleAuthService.loginWithGoogleMobile(tokenId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", loginResponseDto, null));
    }

    @Hidden
    @GetMapping("/google/callback")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletResponse response) throws Exception {
        googleAuthService.googleAuthCallback(code, state, error, response);
    }
}

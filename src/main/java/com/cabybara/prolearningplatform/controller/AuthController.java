package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.email.VerifyOtpRequest;
import com.cabybara.prolearningplatform.dto.request.email.VerifyResetOtpRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.ForgotPasswordRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.LoginRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.LogoutRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.RefreshTokenRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.ResetPasswordRequestDto;
import com.cabybara.prolearningplatform.dto.response.user.GoogleAuthUrlResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.RefreshTokenResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.RegisterResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.VerifyResetOtpResponseDto;
import com.cabybara.prolearningplatform.service.auth.AuthService;
import com.cabybara.prolearningplatform.service.auth.GoogleAuthService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
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
@SecurityRequirements({})
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
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Hidden
    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> registerAdminUser(@RequestBody @Valid RegisterRequestDto registerRequestDto) throws Exception {
        RegisterResponseDto registerResponseDto = authService.registerAdmin(registerRequestDto);
        ApiResponse<RegisterResponseDto> response = ResponseUtil.success("Registration successfully", registerResponseDto, null);
        return ResponseEntity
                .status(HttpStatus.CREATED)
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
            summary = "Login with google user for web application",
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

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestBody VerifyOtpRequest req) {
        authService.verifyEmail(req.getEmail(), req.getOtp());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Email verified successfully", null, null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDto req) {
        authService.forgotPassword(req.getEmail());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Forgot password email sent successfully", null, null));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<VerifyResetOtpResponseDto>> verifyResetOtp(@RequestBody @Valid VerifyResetOtpRequestDto req) {
        String token = authService.verifyResetOtp(req.getEmail(), req.getOtp());
        VerifyResetOtpResponseDto responseData = new VerifyResetOtpResponseDto(token);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("OTP verified successfully", responseData, null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody @Valid ResetPasswordRequestDto req) {
        authService.resetPassword(req.getResetToken(), req.getNewPassword());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Password reset successfully", null, null));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Exchange a valid refresh token for a new access + refresh token pair (rotation)."
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDto>> refresh(
            @Valid @RequestBody RefreshTokenRequestDto req) {
        RefreshTokenResponseDto data = authService.refresh(req.getRefreshToken());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Token refreshed", data, null));
    }

    @Operation(
            summary = "Logout",
            description = "Blacklist current access token and revoke the refresh token. Requires Bearer access token."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequestDto req,
            @AuthenticationPrincipal Jwt jwt) {
        long ttlSeconds = 0L;
        Instant exp = jwt.getExpiresAt();
        if (exp != null) {
            ttlSeconds = Math.max(0L, exp.getEpochSecond() - Instant.now().getEpochSecond());
        }
        authService.logout(jwt.getId(), ttlSeconds, req.getRefreshToken());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Logged out", null, null));
    }

}

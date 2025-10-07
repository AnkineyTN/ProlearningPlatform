package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.*;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.service.AuthService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

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
}

package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.RegisterResponseDto;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AuthService {
    LoginResponseDto authenticateAndGenerateToken(String email, String password);

    RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto) throws Exception;

    RegisterResponseDto registerAdmin(RegisterRequestDto registerRequestDto) throws Exception;

    void googleAuthCallback(String code, String userId, String error, HttpServletResponse response) throws Exception;

    Object loginWithGoogle() throws IOException;
}

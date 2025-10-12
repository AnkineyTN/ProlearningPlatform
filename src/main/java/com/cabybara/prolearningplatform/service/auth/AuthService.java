package com.cabybara.prolearningplatform.service.auth;

import com.cabybara.prolearningplatform.dto.response.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.request.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.response.RegisterResponseDto;

public interface AuthService {
    LoginResponseDto authenticateAndGenerateToken(String email, String password);

    RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto) throws Exception;

    RegisterResponseDto registerAdmin(RegisterRequestDto registerRequestDto) throws Exception;
}

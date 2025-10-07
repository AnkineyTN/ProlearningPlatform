package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.RegisterResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import jakarta.validation.Valid;

public interface AuthService {
    LoginResponseDto authenticateAndGenerateToken(String email, String password);

    RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto) throws Exception;

    RegisterResponseDto registerAdmin(RegisterRequestDto registerRequestDto) throws Exception;
}

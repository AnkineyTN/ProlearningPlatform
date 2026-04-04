package com.cabybara.prolearningplatform.service.auth;

import com.cabybara.prolearningplatform.dto.response.user.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.request.user.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.response.user.RegisterResponseDto;

public interface AuthService {
    LoginResponseDto authenticateAndGenerateToken(String email, String password);

    RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto) throws Exception;

    RegisterResponseDto registerAdmin(RegisterRequestDto registerRequestDto) throws Exception;

    void verifyEmail(String email, String inputOtp);

    void forgotPassword(String email);

    String verifyResetOtp(String email, String inputOtp);

    void resetPassword(String email, String newPassword);
}

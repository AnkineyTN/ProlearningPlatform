package com.cabybara.prolearningplatform.service.auth;

import com.cabybara.prolearningplatform.dto.response.user.GoogleAuthUrlResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.LoginResponseDto;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface GoogleAuthService {

    void googleAuthCallback(String code, String userId, String error, HttpServletResponse response) throws Exception;

    GoogleAuthUrlResponseDto loginWithGoogle() throws IOException;

    LoginResponseDto loginWithGoogleMobile(String token) throws GeneralSecurityException, IOException;

    void refreshGoogleToken(Long userId) throws IOException;
}

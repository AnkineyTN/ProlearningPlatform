package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.GoogleAuthUrlResponseDto;
import com.cabybara.prolearningplatform.dto.LoginResponseDto;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface GoogleAuthService {

    void googleAuthCallback(String code, String userId, String error, HttpServletResponse response) throws Exception;

    GoogleAuthUrlResponseDto loginWithGoogle() throws IOException;

    LoginResponseDto loginWithGoogleMobile(String token) throws GeneralSecurityException, IOException;
}

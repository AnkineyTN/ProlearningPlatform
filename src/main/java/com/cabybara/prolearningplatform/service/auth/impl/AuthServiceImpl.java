package com.cabybara.prolearningplatform.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.cabybara.prolearningplatform.dto.*;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.exception.GoogleAuthException;
import com.cabybara.prolearningplatform.mapper.GoogleAuthItemMapper;
import com.cabybara.prolearningplatform.mapper.UserMapper;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.service.AuthService;
import com.cabybara.prolearningplatform.service.JwtService;
import com.cabybara.prolearningplatform.service.RedisService;
import com.cabybara.prolearningplatform.service.UserService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final UserService userService;
    private final GoogleAuthorizationCodeFlow googleFlow;
    private final RedisService redisService;
    private final GoogleAuthItemMapper googleAuthItemMapper;
    @Value("${spring.security.oauth2.client.frontend_dashboard_url}")
    private String FRONTEND_DASHBOARD_URL;

    @Value("${spring.security.oauth2.client.frontend_login_google_failure_url}")
    private String FRONTEND_LOGIN_GOOGLE_FAILURE_URL;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String REDIRECT_URI;

    @Override
    public LoginResponseDto authenticateAndGenerateToken(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserResponseDto userResponseDto = userMapper.toUserResponseDto((User) authentication.getPrincipal());
        String accessToken = jwtService.generateToken(authentication);

        return LoginResponseDto.builder()
                .userResponseDto(userResponseDto)
                .accessToken(accessToken)
                .build();
    }

    @Override
    public RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto) throws Exception {
        Role role = Role.valueOf(registerRequestDto.getRole());
        UserResponseDto userResponseDto = userService.addUser(
                registerRequestDto,
                role
        );

        return RegisterResponseDto.builder()
                .id(userResponseDto.getId())
                .email(userResponseDto.getEmail())
                .firstName(userResponseDto.getFirstName())
                .lastName(userResponseDto.getLastName())
                .build();
    }

    @Override
    public RegisterResponseDto registerAdmin(RegisterRequestDto registerRequestDto) throws Exception {
        UserResponseDto userResponseDto = userService.addUser(
                registerRequestDto,
                Role.ROLE_ADMIN
        );

        return RegisterResponseDto.builder()
                .id(userResponseDto.getId())
                .email(userResponseDto.getEmail())
                .firstName(userResponseDto.getFirstName())
                .lastName(userResponseDto.getLastName())
                .build();
    }
}

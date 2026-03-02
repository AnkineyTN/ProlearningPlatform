package com.cabybara.prolearningplatform.service.auth.impl;

import com.cabybara.prolearningplatform.dto.request.user.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.response.user.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.RegisterResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.exception.AuthException;
import com.cabybara.prolearningplatform.mapper.GoogleAuthItemMapper;
import com.cabybara.prolearningplatform.mapper.UserMapper;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.service.auth.AuthService;
import com.cabybara.prolearningplatform.service.auth.JwtService;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            UserResponseDto userResponseDto = userMapper.toUserResponseDto((User) authentication.getPrincipal());
            String accessToken = jwtService.generateToken(authentication);

            return LoginResponseDto.builder()
                    .userResponseDto(userResponseDto)
                    .accessToken(accessToken)
                    .build();

        } catch (BadCredentialsException ex) {
            throw new AuthException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
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

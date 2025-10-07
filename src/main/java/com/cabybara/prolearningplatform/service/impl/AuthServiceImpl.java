package com.cabybara.prolearningplatform.service.impl;

import com.cabybara.prolearningplatform.dto.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.RegisterResponseDto;
import com.cabybara.prolearningplatform.dto.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.exception.GoogleAuthException;
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
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    private Credential createAndStoreCredential(String code, String userId) throws IOException {

        try {
            TokenResponse tokenResponse = googleFlow.newTokenRequest(code)
                    .setRedirectUri(REDIRECT_URI)
                    .execute();

            return googleFlow.createAndStoreCredential(tokenResponse, userId);

        } catch (TokenResponseException e) {
            throw new GoogleAuthException(e.getMessage());
        }
    }

    @Override
    public Object loginWithGoogle() throws IOException {
        String randomState = UUID.randomUUID().toString();
        redisService.set(randomState, "tempStateGoogleAuth", 60);

        String authorizationUrl = googleFlow.newAuthorizationUrl()
                .setRedirectUri(REDIRECT_URI)
                .setState(randomState)
                .build();

        Map<String, String> responseData = new HashMap<>();
        responseData.put("AuthorizationUrl", authorizationUrl);
        return responseData;
    }

    @Override
    public void googleAuthCallback(String code, String state, String error, HttpServletResponse response) throws Exception {
        if (error != null) {
            response.sendRedirect(FRONTEND_LOGIN_GOOGLE_FAILURE_URL + "&reason=" + error);
            return;
        }

        Credential credential = createAndStoreCredential(code, state);
        StoredCredential tempCredential = new StoredCredential(credential);

        if (credential.getAccessToken() == null) {
            response.sendRedirect(FRONTEND_LOGIN_GOOGLE_FAILURE_URL + "&reason=credential_null");
            return;
        }

        Oauth2 oauth2 = new Oauth2.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Web client 1").build();

        Userinfo userInfo = oauth2.userinfo().get().execute();

        User user = userService.findOrCreateFromGoogle(userInfo);
        if (googleFlow.loadCredential(user.getId().toString()) == null) {
            googleFlow.getCredentialDataStore().set(String.valueOf(user.getId()), tempCredential);
        }
        googleFlow.getCredentialDataStore().delete(state);

        String accessToken = jwtService.generateToken(userInfo.getEmail());

        response.sendRedirect(FRONTEND_DASHBOARD_URL + "&accessToken=" + accessToken);
    }
}

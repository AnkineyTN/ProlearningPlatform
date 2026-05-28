package com.cabybara.prolearningplatform.service.auth.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.cabybara.prolearningplatform.dto.response.user.GoogleAuthUrlResponseDto;
import com.cabybara.prolearningplatform.dto.helper.GoogleUserInfoDto;
import com.cabybara.prolearningplatform.dto.response.user.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.exception.GoogleAuthException;
import com.cabybara.prolearningplatform.mapper.UserMapper;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.service.auth.GoogleAuthService;
import com.cabybara.prolearningplatform.service.auth.JwtService;
import com.cabybara.prolearningplatform.service.auth.RefreshTokenService;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements GoogleAuthService {
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
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
    public GoogleAuthUrlResponseDto loginWithGoogle() throws IOException {
        String randomState = UUID.randomUUID().toString();
        redisService.set(randomState, "tempStateGoogleAuth", 60);

        String authorizationUrl = googleFlow.newAuthorizationUrl()
                .setRedirectUri(REDIRECT_URI)
                .setState(randomState)
                .build();

        return GoogleAuthUrlResponseDto.builder()
                .authorizationUrl(authorizationUrl)
                .build();
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

        if (!user.isEmailVerified()) {
            userService.verifyEmail(user.getId());
        }

        googleFlow.getCredentialDataStore().delete(state);

        String accessToken = jwtService.generateToken(userInfo.getEmail());
        String refreshToken = refreshTokenService.issue(user.getId());

        response.sendRedirect(FRONTEND_DASHBOARD_URL
                + "&accessToken=" + accessToken
                + "&refreshToken=" + refreshToken);
    }

    @Override
    public LoginResponseDto loginWithGoogleMobile(String token) throws GeneralSecurityException, IOException {
        DecodedJWT jwt = jwtService.decodeGoogleIdToken(token);
        GoogleUserInfoDto googleUserInfoDto = GoogleUserInfoDto.builder()
                .sub(jwt.getSubject())
                .email(jwt.getClaim("email").asString())
                .emailVerified(jwt.getClaim("email_verified").asBoolean())
                .name(jwt.getClaim("name").asString())
                .picture(jwt.getClaim("picture").asString())
                .givenName(jwt.getClaim("given_name").asString())
                .familyName(jwt.getClaim("family_name").asString())
                .build();

        User user = userService.findOrCreateFromGoogle(googleUserInfoDto);

        if (!user.isEmailVerified()) {
            userService.verifyEmail(user.getId());
        }

        UserResponseDto userResponseDto = userMapper.toUserResponseDto(user);
        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.issue(user.getId());
        return LoginResponseDto.builder()
                .userResponseDto(userResponseDto)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void refreshGoogleToken(Long userId) throws IOException {
        Credential credential = googleFlow.loadCredential(userId.toString());

        if (credential == null) {
            throw new GoogleAuthException("No Google credential found for user: " + userId);
        }

        Long expiresIn = credential.getExpiresInSeconds();
        if (expiresIn == null || expiresIn <= 60) {
            try {
                boolean refreshed = credential.refreshToken();
                if (!refreshed) {
                    throw new GoogleAuthException("Failed to refresh Google token: refresh did not occur");
                }
            } catch (TokenResponseException e) {
                throw new GoogleAuthException("Failed to refresh Google token: " + e.getMessage());
            }
        }
    }
}

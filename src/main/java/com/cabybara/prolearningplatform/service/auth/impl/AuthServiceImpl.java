package com.cabybara.prolearningplatform.service.auth.impl;

import com.cabybara.prolearningplatform.dto.request.user.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.response.user.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.RefreshTokenResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.RegisterResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.exception.AuthException;
import com.cabybara.prolearningplatform.exception.EmailNotVerifiedException;
import com.cabybara.prolearningplatform.exception.OtpException;
import com.cabybara.prolearningplatform.mapper.GoogleAuthItemMapper;
import com.cabybara.prolearningplatform.mapper.UserMapper;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.service.auth.AuthService;
import com.cabybara.prolearningplatform.service.auth.JwtService;
import com.cabybara.prolearningplatform.service.auth.RefreshTokenService;
import com.cabybara.prolearningplatform.service.email.EmailService;
import com.cabybara.prolearningplatform.service.otp.OtpService;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.cabybara.prolearningplatform.exception.AccountBlockedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
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

    private final OtpService     otpService;
    private final EmailService   emailService;

    @Override
    public LoginResponseDto authenticateAndGenerateToken(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            User user = (User) authentication.getPrincipal();
            UserResponseDto userResponseDto = userMapper.toUserResponseDto(user);
            String accessToken = jwtService.generateToken(authentication);
            String refreshToken = refreshTokenService.issue(user.getId());

            return LoginResponseDto.builder()
                    .userResponseDto(userResponseDto)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (LockedException ex) {
            throw new AccountBlockedException("ACCOUNT_BLOCKED");
        } catch (BadCredentialsException ex) {
            throw new AuthException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @Override
    public RefreshTokenResponseDto refresh(String refreshToken) {
        Long userId = refreshTokenService.validateAndConsume(refreshToken);
        User user = userService.getUserById(userId);

        String newAccess = jwtService.generateToken(user.getUsername());
        String newRefresh = refreshTokenService.issue(userId);

        return RefreshTokenResponseDto.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .build();
    }

    @Override
    public void logout(String accessTokenJti, long accessTokenTtlSeconds, String refreshToken) {
        if (accessTokenJti != null) {
            jwtService.blacklistAccessTokenByJti(accessTokenJti, accessTokenTtlSeconds);
        }
        if (refreshToken != null) {
            refreshTokenService.revoke(refreshToken);
        }
    }

    @Override
    @Transactional
    public RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto) throws Exception {
        Role role = Role.valueOf(registerRequestDto.getRole());
        UserResponseDto userResponseDto = userService.addUser(
                registerRequestDto,
                role
        );

        String otp = otpService.generateVerifyOtp(userResponseDto.getId());
        String fullName = userResponseDto.getFirstName() + " " + userResponseDto.getLastName();
        emailService.sendVerifyOtp(userResponseDto.getEmail(), fullName, otp);

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

    @Override
    @Transactional
    public void verifyEmail(String email, String inputOtp) {
        User user = userService.getUserByEmail(email);

        otpService.verifyVerifyOtp(user.getId(), inputOtp);

        userService.verifyEmail(user.getId());
    }

    @Override
    public void forgotPassword(String email) {
        User user = userService.getUserByEmail(email);

        if (!user.isEmailVerified()) {
            String otp = otpService.generateVerifyOtp(user.getId());
            emailService.sendVerifyOtp(user.getEmail(), user.getUsername(), otp);
            throw new EmailNotVerifiedException();
        }

        String otp = otpService.generateResetOtp(user.getId());
        emailService.sendResetOtp(user.getEmail(), user.getUsername(), otp);
    }

    @Override
    public String verifyResetOtp(String email, String inputOtp) {
        User user = userService.getUserByEmail(email);

        otpService.verifyResetOtp(user.getId(), inputOtp);

        return otpService.generateResetToken(user.getId());
    }

    @Override
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        Long userId = otpService.validateAndConsumeResetToken(resetToken);

        User user = userService.getUserById(userId);

        userService.resetPassword(userId, newPassword);

        refreshTokenService.revokeAllForUser(userId);

        emailService.sendPasswordChangedNotification(user.getEmail(), user.getUsername());
    }
}

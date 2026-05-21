package com.cabybara.prolearningplatform.service.auth;

import com.cabybara.prolearningplatform.dto.request.user.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.response.user.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.RefreshTokenResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.RegisterResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.exception.AccountBlockedException;
import com.cabybara.prolearningplatform.exception.AuthException;
import com.cabybara.prolearningplatform.exception.EmailNotVerifiedException;
import com.cabybara.prolearningplatform.mapper.GoogleAuthItemMapper;
import com.cabybara.prolearningplatform.mapper.UserMapper;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.service.auth.impl.AuthServiceImpl;
import com.cabybara.prolearningplatform.service.email.EmailService;
import com.cabybara.prolearningplatform.service.otp.OtpService;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserService userService;
    @Mock
    private GoogleAuthorizationCodeFlow googleFlow;
    @Mock
    private RedisService redisService;
    @Mock
    private GoogleAuthItemMapper googleAuthItemMapper;
    @Mock
    private OtpService otpService;
    @Mock
    private EmailService emailService;

    @Test
    void authenticateAndGenerateTokenReturnsAccessAndRefreshTokens() {
        AuthServiceImpl service = new AuthServiceImpl(authenticationManager, jwtService, refreshTokenService, userMapper, userService,
                googleFlow, redisService, googleAuthItemMapper, otpService, emailService);
        User user = TestFixtures.user(1L);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        UserResponseDto mappedUser = UserResponseDto.builder().id(1L).email(user.getEmail()).build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userMapper.toUserResponseDto(user)).thenReturn(mappedUser);
        when(jwtService.generateToken(authentication)).thenReturn("access");
        when(refreshTokenService.issue(1L)).thenReturn("refresh");

        LoginResponseDto response = service.authenticateAndGenerateToken(user.getEmail(), "secret");

        assertSame(mappedUser, response.getUserResponseDto());
        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void authenticateAndGenerateTokenMapsLockedAndBadCredentialsExceptions() {
        AuthServiceImpl service = new AuthServiceImpl(authenticationManager, jwtService, refreshTokenService, userMapper, userService,
                googleFlow, redisService, googleAuthItemMapper, otpService, emailService);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new LockedException("locked"))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(AccountBlockedException.class, () -> service.authenticateAndGenerateToken("a@b.com", "x"));

        AuthException authException = assertThrows(AuthException.class, () -> service.authenticateAndGenerateToken("a@b.com", "x"));
        assertEquals(HttpStatus.BAD_REQUEST, authException.getStatus());
        assertEquals("bad credentials", authException.getMessage());
    }

    @Test
    void refreshConsumesOldTokenAndIssuesNewPair() {
        AuthServiceImpl service = new AuthServiceImpl(authenticationManager, jwtService, refreshTokenService, userMapper, userService,
                googleFlow, redisService, googleAuthItemMapper, otpService, emailService);
        User user = TestFixtures.user(2L);

        when(refreshTokenService.validateAndConsume("refresh-old")).thenReturn(2L);
        when(userService.getUserById(2L)).thenReturn(user);
        when(jwtService.generateToken(user.getUsername())).thenReturn("access-new");
        when(refreshTokenService.issue(2L)).thenReturn("refresh-new");

        RefreshTokenResponseDto response = service.refresh("refresh-old");

        assertEquals("access-new", response.getAccessToken());
        assertEquals("refresh-new", response.getRefreshToken());
    }

    @Test
    void logoutBlacklistsAccessTokenAndRevokesRefreshTokenWhenPresent() {
        AuthServiceImpl service = new AuthServiceImpl(authenticationManager, jwtService, refreshTokenService, userMapper, userService,
                googleFlow, redisService, googleAuthItemMapper, otpService, emailService);

        service.logout("jti-1", 60L, "refresh-1");
        service.logout(null, 60L, null);

        verify(jwtService).blacklistAccessTokenByJti("jti-1", 60L);
        verify(refreshTokenService).revoke("refresh-1");
    }

    @Test
    void registerUserCreatesUserGeneratesOtpAndSendsVerifyEmail() throws Exception {
        AuthServiceImpl service = new AuthServiceImpl(authenticationManager, jwtService, refreshTokenService, userMapper, userService,
                googleFlow, redisService, googleAuthItemMapper, otpService, emailService);
        RegisterRequestDto request = new RegisterRequestDto("First", "Last", "secret12", "user@example.com", "ROLE_USER");
        UserResponseDto userResponse = UserResponseDto.builder()
                .id(5L)
                .firstName("First")
                .lastName("Last")
                .email("user@example.com")
                .build();

        when(userService.addUser(request, Role.ROLE_USER)).thenReturn(userResponse);
        when(otpService.generateVerifyOtp(5L)).thenReturn("123456");

        RegisterResponseDto response = service.registerUser(request);

        verify(emailService).sendVerifyOtp("user@example.com", "First Last", "123456");
        assertEquals(5L, response.getId());
    }

    @Test
    void registerAdminUsesAdminRoleWithoutOtpEmail() throws Exception {
        AuthServiceImpl service = new AuthServiceImpl(authenticationManager, jwtService, refreshTokenService, userMapper, userService,
                googleFlow, redisService, googleAuthItemMapper, otpService, emailService);
        RegisterRequestDto request = new RegisterRequestDto("Admin", "User", "secret12", "admin@example.com", "ROLE_USER");
        UserResponseDto userResponse = UserResponseDto.builder().id(8L).email("admin@example.com").firstName("Admin").lastName("User").build();

        when(userService.addUser(request, Role.ROLE_ADMIN)).thenReturn(userResponse);

        RegisterResponseDto response = service.registerAdmin(request);

        verify(otpService, never()).generateVerifyOtp(any());
        verify(emailService, never()).sendVerifyOtp(any(), any(), any());
        assertEquals(8L, response.getId());
    }

    @Test
    void verifyEmailLoadsUserVerifiesOtpAndMarksEmailVerified() {
        AuthServiceImpl service = new AuthServiceImpl(authenticationManager, jwtService, refreshTokenService, userMapper, userService,
                googleFlow, redisService, googleAuthItemMapper, otpService, emailService);
        User user = TestFixtures.user(9L);

        when(userService.getUserByEmail("user9@example.com")).thenReturn(user);

        service.verifyEmail("user9@example.com", "123456");

        verify(otpService).verifyVerifyOtp(9L, "123456");
        verify(userService).verifyEmail(9L);
    }

    @Test
    void forgotPasswordForUnverifiedEmailResendsVerifyOtpAndThrows() {
        AuthServiceImpl service = new AuthServiceImpl(authenticationManager, jwtService, refreshTokenService, userMapper, userService,
                googleFlow, redisService, googleAuthItemMapper, otpService, emailService);
        User user = TestFixtures.user(10L);
        user.setEmailVerified(false);

        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(otpService.generateVerifyOtp(10L)).thenReturn("111111");

        assertThrows(EmailNotVerifiedException.class, () -> service.forgotPassword(user.getEmail()));

        verify(emailService).sendVerifyOtp(user.getEmail(), user.getUsername(), "111111");
        verify(otpService, never()).generateResetOtp(any());
    }

    @Test
    void forgotPasswordForVerifiedEmailSendsResetOtp() {
        AuthServiceImpl service = new AuthServiceImpl(authenticationManager, jwtService, refreshTokenService, userMapper, userService,
                googleFlow, redisService, googleAuthItemMapper, otpService, emailService);
        User user = TestFixtures.user(11L);
        user.setEmailVerified(true);

        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(otpService.generateResetOtp(11L)).thenReturn("222222");

        service.forgotPassword(user.getEmail());

        verify(emailService).sendResetOtp(user.getEmail(), user.getUsername(), "222222");
    }

    @Test
    void verifyResetOtpReturnsGeneratedResetToken() {
        AuthServiceImpl service = new AuthServiceImpl(authenticationManager, jwtService, refreshTokenService, userMapper, userService,
                googleFlow, redisService, googleAuthItemMapper, otpService, emailService);
        User user = TestFixtures.user(12L);
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(otpService.generateResetToken(12L)).thenReturn("reset-token");

        String token = service.verifyResetOtp(user.getEmail(), "333333");

        verify(otpService).verifyResetOtp(12L, "333333");
        assertEquals("reset-token", token);
    }

    @Test
    void resetPasswordResetsUserPasswordRevokesTokensAndSendsNotification() {
        AuthServiceImpl service = new AuthServiceImpl(authenticationManager, jwtService, refreshTokenService, userMapper, userService,
                googleFlow, redisService, googleAuthItemMapper, otpService, emailService);
        User user = TestFixtures.user(13L);

        when(otpService.validateAndConsumeResetToken("reset-token")).thenReturn(13L);
        when(userService.getUserById(13L)).thenReturn(user);

        service.resetPassword("reset-token", "new-pass");

        verify(userService).resetPassword(13L, "new-pass");
        verify(refreshTokenService).revokeAllForUser(13L);
        verify(emailService).sendPasswordChangedNotification(user.getEmail(), user.getUsername());
    }
}

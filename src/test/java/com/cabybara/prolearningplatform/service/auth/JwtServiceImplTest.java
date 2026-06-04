package com.cabybara.prolearningplatform.service.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.model.Authority;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.service.auth.impl.JwtServiceImpl;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    private RedisService redisService;

    @Mock
    private UserService userService;

    @Test
    void generateTokenFromAuthenticationEmbedsEmailAndUserId() {
        JwtServiceImpl service = new JwtServiceImpl(redisService, userService);
        ReflectionTestUtils.setField(service, "JWT_SECRET", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(service, "JWT_EXPIRATION", 60_000L);
        User user = TestFixtures.user(7L);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = service.generateToken(authentication);

        assertNotNull(token);
        assertEquals("user7@example.com", service.extractEmail(token));
        assertEquals(7L, service.extractUserId(token));
        assertTrue(service.validateToken(token));
    }

    @Test
    void generateTokenFromUsernameUsesLoadedUserRoles() {
        JwtServiceImpl service = new JwtServiceImpl(redisService, userService);
        ReflectionTestUtils.setField(service, "JWT_SECRET", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(service, "JWT_EXPIRATION", 60_000L);
        User user = TestFixtures.user(9L);
        user.setRoles(Set.of(Authority.builder().user(user).authority(Role.ROLE_ADMIN).build()));
        when(userService.loadUserByUsername("user9@example.com")).thenReturn(user);

        String token = service.generateToken("user9@example.com");

        assertEquals("user9@example.com", service.extractEmail(token));
        assertEquals(9L, service.extractUserId(token));
    }

    @Test
    void validateTokenReturnsFalseWhenBlacklisted() {
        JwtServiceImpl service = new JwtServiceImpl(redisService, userService);
        ReflectionTestUtils.setField(service, "JWT_SECRET", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(service, "JWT_EXPIRATION", 60_000L);
        User user = TestFixtures.user(10L);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = service.generateToken(authentication);
        DecodedJWT decoded = service.decodeGoogleIdToken(token);
        when(redisService.hasKey(JwtServiceImpl.BLACKLIST_JTI_KEY_PREFIX + decoded.getId())).thenReturn(true);

        assertFalse(service.validateToken(token));
    }

    @Test
    void validateTokenReturnsFalseWhenExpired() throws InterruptedException {
        JwtServiceImpl service = new JwtServiceImpl(redisService, userService);
        ReflectionTestUtils.setField(service, "JWT_SECRET", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(service, "JWT_EXPIRATION", 1L);
        User user = TestFixtures.user(11L);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = service.generateToken(authentication);

        Thread.sleep(10L);

        assertFalse(service.validateToken(token));
    }

    @Test
    void blacklistAccessTokenByJtiSkipsInvalidInputAndStoresValidInput() {
        JwtServiceImpl service = new JwtServiceImpl(redisService, userService);

        service.blacklistAccessTokenByJti(null, 10);
        service.blacklistAccessTokenByJti("jti-1", 0);
        service.blacklistAccessTokenByJti("jti-2", 30);

        verify(redisService).set(JwtServiceImpl.BLACKLIST_JTI_KEY_PREFIX + "jti-2", "1", 30);
    }

    @Test
    void decodeGoogleIdTokenDecodesWithoutVerification() {
        JwtServiceImpl service = new JwtServiceImpl(redisService, userService);
        ReflectionTestUtils.setField(service, "JWT_SECRET", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(service, "JWT_EXPIRATION", 60_000L);
        User user = TestFixtures.user(12L);
        String token = service.generateToken(new UsernamePasswordAuthenticationToken(
                user,
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));

        DecodedJWT decoded = service.decodeGoogleIdToken(token);

        assertEquals("user12@example.com", decoded.getSubject());
        assertNotNull(decoded.getId());
    }
}

package com.cabybara.prolearningplatform.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationContextTest {

    private final AuthenticationContext authenticationContext = new AuthenticationContext();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserIdReturnsLongClaim() {
        setAuthenticationWithClaim(123L);

        assertEquals(123L, authenticationContext.getCurrentUserId());
    }

    @Test
    void getCurrentUserIdConvertsIntegerClaim() {
        setAuthenticationWithClaim(456);

        assertEquals(456L, authenticationContext.getCurrentUserId());
    }

    @Test
    void getCurrentUserIdParsesStringClaim() {
        setAuthenticationWithClaim("789");

        assertEquals(789L, authenticationContext.getCurrentUserId());
    }

    @Test
    void getCurrentUserIdFailsWhenAuthenticationMissing() {
        AuthenticationCredentialsNotFoundException exception = assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                authenticationContext::getCurrentUserId
        );

        assertEquals("User is not authenticated", exception.getMessage());
    }

    @Test
    void getCurrentUserIdFailsWhenPrincipalIsAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(newAuthenticatedToken("anonymousUser"));

        AuthenticationCredentialsNotFoundException exception = assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                authenticationContext::getCurrentUserId
        );

        assertEquals("User is not authenticated", exception.getMessage());
    }

    @Test
    void getCurrentUserIdFailsWhenPrincipalIsNotJwt() {
        SecurityContextHolder.getContext().setAuthentication(newAuthenticatedToken("plain-user"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                authenticationContext::getCurrentUserId
        );

        assertEquals("Authentication principal is not a JWT", exception.getMessage());
    }

    @Test
    void getCurrentUserIdFailsWhenClaimMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("email", "user@example.com")
                .build();

        SecurityContextHolder.getContext().setAuthentication(newAuthenticatedToken(jwt));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                authenticationContext::getCurrentUserId
        );

        assertEquals("User ID (id) claim is missing from JWT", exception.getMessage());
    }

    @Test
        void getCurrentUserIdFailsWhenClaimTypeUnexpected() {
        setAuthenticationWithClaim(Map.of("nested", 1));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                authenticationContext::getCurrentUserId
        );

        assertTrue(exception.getMessage().startsWith("User ID (id) claim is of an unexpected type:"));
    }

    private void setAuthenticationWithClaim(Object claimValue) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("id", claimValue)
                .build();

        SecurityContextHolder.getContext().setAuthentication(newAuthenticatedToken(jwt));
    }

    private TestingAuthenticationToken newAuthenticatedToken(Object principal) {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(principal, null, "ROLE_USER");
        authentication.setAuthenticated(true);
        return authentication;
    }
}

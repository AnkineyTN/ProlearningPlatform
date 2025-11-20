package com.cabybara.prolearningplatform.utils;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationContext {
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public Long getCurrentUserId() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof Jwt)) {
            throw new IllegalStateException("Authentication principal is not a JWT");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Object userIdClaim = jwt.getClaims().get("id");

        if (userIdClaim == null) {
            throw new IllegalStateException("User ID (id) claim is missing from JWT");
        }

        if (userIdClaim instanceof Long) {
            return (Long) userIdClaim;
        } else if (userIdClaim instanceof Integer) {
            return ((Integer) userIdClaim).longValue();
        } else if (userIdClaim instanceof String) {
            return Long.parseLong((String) userIdClaim);
        }

        throw new IllegalStateException("User ID (id) claim is of an unexpected type: " + userIdClaim.getClass().getName());
    }
}

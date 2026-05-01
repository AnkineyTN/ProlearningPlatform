package com.cabybara.prolearningplatform.service.auth;

public interface RefreshTokenService {
    String issue(Long userId);

    Long validateAndConsume(String token);

    void revoke(String token);

    void revokeAllForUser(Long userId);
}

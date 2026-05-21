package com.cabybara.prolearningplatform.service.auth;

import com.cabybara.prolearningplatform.exception.AuthException;
import com.cabybara.prolearningplatform.service.auth.impl.RefreshTokenServiceImpl;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RedisService redisService;

    @Test
    void issueStoresTokenAndUserIndexWithConfiguredTtl() {
        RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(redisService);
        ReflectionTestUtils.setField(service, "refreshTtlMs", 120_000L);

        String token = service.issue(7L);

        assertNotNull(token);
        assertFalse(token.isBlank());
        verify(redisService).set("auth:refresh:" + token, "7", 120L);
        verify(redisService).sAdd("auth:refresh-index:7", token);
        verify(redisService).expire("auth:refresh-index:7", 120L);
    }

    @Test
    void validateAndConsumeDeletesTokenAndRemovesFromIndex() {
        RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(redisService);
        when(redisService.get("auth:refresh:token-1")).thenReturn("9");

        Long userId = service.validateAndConsume("token-1");

        assertEquals(9L, userId);
        verify(redisService).delete("auth:refresh:token-1");
        verify(redisService).sRemove("auth:refresh-index:9", "token-1");
    }

    @Test
    void validateAndConsumeThrowsWhenTokenMissing() {
        RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(redisService);
        when(redisService.get(anyString())).thenReturn(null);

        AuthException exception = assertThrows(AuthException.class, () -> service.validateAndConsume("missing"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Invalid or expired refresh token", exception.getMessage());
    }

    @Test
    void revokeDoesNothingWhenTokenMissing() {
        RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(redisService);
        when(redisService.get("auth:refresh:missing")).thenReturn(null);

        service.revoke("missing");

        verify(redisService, never()).delete("auth:refresh:missing");
    }

    @Test
    void revokeDeletesTokenAndUpdatesUserIndex() {
        RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(redisService);
        when(redisService.get("auth:refresh:token-2")).thenReturn("5");

        service.revoke("token-2");

        verify(redisService).delete("auth:refresh:token-2");
        verify(redisService).sRemove("auth:refresh-index:5", "token-2");
    }

    @Test
    void revokeAllForUserDeletesAllTokensAndIndex() {
        RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(redisService);
        when(redisService.sMembers("auth:refresh-index:4")).thenReturn(Set.of("a", "b"));
        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);

        service.revokeAllForUser(4L);

        verify(redisService).delete(captor.capture());
        verify(redisService).delete("auth:refresh-index:4");
        assertEquals(2, captor.getValue().size());
        assertTrue(captor.getValue().contains("auth:refresh:a"));
        assertTrue(captor.getValue().contains("auth:refresh:b"));
    }

    @Test
    void revokeAllForUserSkipsDeleteWhenIndexEmpty() {
        RefreshTokenServiceImpl service = new RefreshTokenServiceImpl(redisService);
        when(redisService.sMembers("auth:refresh-index:4")).thenReturn(Set.of());

        service.revokeAllForUser(4L);

        verify(redisService, never()).delete(org.mockito.ArgumentMatchers.any(Collection.class));
        verify(redisService, never()).delete("auth:refresh-index:4");
    }
}

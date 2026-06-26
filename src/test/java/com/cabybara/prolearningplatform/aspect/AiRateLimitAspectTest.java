package com.cabybara.prolearningplatform.aspect;

import com.cabybara.prolearningplatform.exception.RateLimitExceededException;
import com.cabybara.prolearningplatform.service.permission.AccountPermissionService;
import com.cabybara.prolearningplatform.service.permission.annotation.AiRateLimit;
import com.cabybara.prolearningplatform.service.permission.aspect.AiRateLimitAspect;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiRateLimitAspectTest {

    @Mock
    private RedisService redisService;

    @Mock
    private AccountPermissionService accountPermissionService;

    @Mock
    private Environment env;

    @Mock
    private JoinPoint joinPoint;

    private StubAuthenticationContext authenticationContext;
    private AiRateLimitAspect aspect;

    @BeforeEach
    void setUp() {
        authenticationContext = new StubAuthenticationContext();
        aspect = new AiRateLimitAspect(redisService, authenticationContext, accountPermissionService, env);
    }

    @Test
    void checkRateLimitAllowsWithinLimit() throws Exception {
        authenticationContext.setMockUserId(1L);
        when(accountPermissionService.isPro(1L)).thenReturn(false);

        when(env.getProperty("app.rate-limit.ai-generation.free-limit", Integer.class)).thenReturn(5);
        when(env.getProperty("app.rate-limit.ai-generation.period-seconds", Long.class)).thenReturn(86400L);

        String key = "ratelimit:1:AI_GENERATION";
        when(redisService.get(key)).thenReturn(null);

        assertDoesNotThrow(() -> aspect.checkRateLimit(joinPoint, aiRateLimitAnnotation()));

        verify(redisService).set(key, 1, 86400L);
    }

    @Test
    void checkRateLimitAllowsWhenIncremented() throws Exception {
        authenticationContext.setMockUserId(1L);
        when(accountPermissionService.isPro(1L)).thenReturn(false);

        when(env.getProperty("app.rate-limit.ai-generation.free-limit", Integer.class)).thenReturn(5);
        when(env.getProperty("app.rate-limit.ai-generation.period-seconds", Long.class)).thenReturn(86400L);

        String key = "ratelimit:1:AI_GENERATION";
        when(redisService.get(key)).thenReturn(3);
        when(redisService.getTTL(key)).thenReturn(5000L);

        assertDoesNotThrow(() -> aspect.checkRateLimit(joinPoint, aiRateLimitAnnotation()));

        verify(redisService).set(key, 4, 5000L);
    }

    @Test
    void checkRateLimitThrowsWhenExceeded() throws Exception {
        authenticationContext.setMockUserId(1L);
        when(accountPermissionService.isPro(1L)).thenReturn(false);

        when(env.getProperty("app.rate-limit.ai-generation.free-limit", Integer.class)).thenReturn(5);
        when(env.getProperty("app.rate-limit.ai-generation.period-seconds", Long.class)).thenReturn(86400L);

        String key = "ratelimit:1:AI_GENERATION";
        when(redisService.get(key)).thenReturn(5);
        when(redisService.getTTL(key)).thenReturn(5000L);

        assertThrows(RateLimitExceededException.class,
                () -> aspect.checkRateLimit(joinPoint, aiRateLimitAnnotation()));
    }

    private AiRateLimit aiRateLimitAnnotation() throws NoSuchMethodException {
        Method method = TestHandler.class.getDeclaredMethod("handle");
        return method.getAnnotation(AiRateLimit.class);
    }

    private static class StubAuthenticationContext extends AuthenticationContext {
        private Long mockUserId;

        public void setMockUserId(Long mockUserId) {
            this.mockUserId = mockUserId;
        }

        @Override
        public Long getCurrentUserId() {
            if (mockUserId == null) {
                throw new RuntimeException("No mock user ID set");
            }
            return mockUserId;
        }
    }

    private static class TestHandler {
        @AiRateLimit(type = "AI_GENERATION")
        void handle() {
        }
    }
}

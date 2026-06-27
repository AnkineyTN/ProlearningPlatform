package com.cabybara.prolearningplatform.service.permission;

import com.cabybara.prolearningplatform.dto.response.user.AiUsageResponseDto;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
import com.cabybara.prolearningplatform.service.permission.impl.AiUsageServiceImpl;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiUsageServiceImplTest {

    @Mock
    private RedisService redisService;

    @Mock
    private AccountPermissionService accountPermissionService;

    @Mock
    private UserLlmConfigService userLlmConfigService;

    @Mock
    private Environment env;

    private AiUsageServiceImpl aiUsageService;

    @BeforeEach
    void setUp() {
        aiUsageService = new AiUsageServiceImpl(redisService, accountPermissionService, userLlmConfigService, env);
    }

    @Test
    void getAiUsageReturnsCorrectUsageForFreeUser() {
        Long userId = 1L;
        when(accountPermissionService.isPro(userId)).thenReturn(false);
        when(userLlmConfigService.getDecryptedConfig(userId)).thenReturn(null);

        // Env mocks for generation
        when(env.getProperty("app.rate-limit.ai-generation.free-limit", Integer.class)).thenReturn(5);
        when(env.getProperty("app.rate-limit.ai-generation.period-seconds", Long.class)).thenReturn(86400L);

        // Env mocks for interactive
        when(env.getProperty("app.rate-limit.ai-interactive.free-limit", Integer.class)).thenReturn(20);
        when(env.getProperty("app.rate-limit.ai-interactive.period-seconds", Long.class)).thenReturn(86400L);

        String genKey = "ratelimit:1:AI_GENERATION";
        String interKey = "ratelimit:1:AI_INTERACTIVE";

        when(redisService.get(genKey)).thenReturn(2);
        when(redisService.getTTL(genKey)).thenReturn(3600L);

        when(redisService.get(interKey)).thenReturn(null);
        when(redisService.getTTL(interKey)).thenReturn(null);

        AiUsageResponseDto response = aiUsageService.getAiUsage(userId);

        assertNotNull(response);
        assertEquals("FREE", response.getTier());
        assertFalse(response.isByokActive());

        // Gen detail
        assertEquals(5, response.getGeneration().getLimit());
        assertEquals(2, response.getGeneration().getUsed());
        assertEquals(3, response.getGeneration().getRemaining());
        assertEquals(3600L, response.getGeneration().getResetTimeSeconds());

        // Interactive detail
        assertEquals(20, response.getInteractive().getLimit());
        assertEquals(0, response.getInteractive().getUsed());
        assertEquals(20, response.getInteractive().getRemaining());
        assertEquals(0L, response.getInteractive().getResetTimeSeconds());
    }

    @Test
    void getAiUsageReturnsCorrectUsageWhenByokActive() {
        Long userId = 1L;
        when(accountPermissionService.isPro(userId)).thenReturn(true);
        com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig mockConfig = 
                new com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig(
                        com.cabybara.prolearningplatform.enums.LlmProvider.GOOGLE, "gemini-1.5", "someKey");
        when(userLlmConfigService.getDecryptedConfig(userId)).thenReturn(mockConfig);

        AiUsageResponseDto response = aiUsageService.getAiUsage(userId);

        assertNotNull(response);
        assertEquals("PRO", response.getTier());
        assertTrue(response.isByokActive());

        // Gen detail should have limits and remaining set to -1 (unlimited)
        assertEquals(-1, response.getGeneration().getLimit());
        assertEquals(0, response.getGeneration().getUsed());
        assertEquals(-1, response.getGeneration().getRemaining());
        assertEquals(0L, response.getGeneration().getResetTimeSeconds());

        // Interactive detail should have limits and remaining set to -1 (unlimited)
        assertEquals(-1, response.getInteractive().getLimit());
        assertEquals(0, response.getInteractive().getUsed());
        assertEquals(-1, response.getInteractive().getRemaining());
        assertEquals(0L, response.getInteractive().getResetTimeSeconds());
    }
}

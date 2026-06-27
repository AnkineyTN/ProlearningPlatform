package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.user.AiUsageResponseDto;
import com.cabybara.prolearningplatform.service.permission.AiUsageService;
import com.cabybara.prolearningplatform.service.permission.impl.NotePermissionService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.cabybara.prolearningplatform.support.WebMvcTestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({WebMvcTestSecurityConfig.class, UserControllerWebMvcTest.TestConfig.class})
@ActiveProfiles("test")
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AiUsageService aiUsageService;

    @TestConfiguration
    static class TestConfig {
        @Bean("notePermissionService")
        public NotePermissionService notePermissionService() {
            return new NotePermissionService(null, null, null, null, null) {
                @Override
                public boolean hasAccess(Long userId, Long resourceId) {
                    return true;
                }
            };
        }

        @Bean
        public AuthenticationContext authenticationContext() {
            return new AuthenticationContext() {
                @Override
                public Long getCurrentUserId() {
                    return 123L;
                }
            };
        }
    }

    @Test
    void getAiUsageWithoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(get("/users/me/ai-usage"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAiUsageReturns200WithCorrectJson() throws Exception {
        AiUsageResponseDto usage = AiUsageResponseDto.builder()
                .tier("FREE")
                .byokActive(false)
                .generation(AiUsageResponseDto.UsageDetail.builder()
                        .limit(5)
                        .used(1)
                        .remaining(4)
                        .resetTimeSeconds(12345L)
                        .build())
                .interactive(AiUsageResponseDto.UsageDetail.builder()
                        .limit(20)
                        .used(5)
                        .remaining(15)
                        .resetTimeSeconds(54321L)
                        .build())
                .build();

        when(aiUsageService.getAiUsage(123L)).thenReturn(usage);

        mockMvc.perform(get("/users/me/ai-usage")
                        .with(jwt().jwt(builder -> builder.claim("id", 123L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Get AI usage status successfully"))
                .andExpect(jsonPath("$.data.tier").value("FREE"))
                .andExpect(jsonPath("$.data.byokActive").value(false))
                .andExpect(jsonPath("$.data.generation.limit").value(5))
                .andExpect(jsonPath("$.data.generation.used").value(1))
                .andExpect(jsonPath("$.data.generation.remaining").value(4))
                .andExpect(jsonPath("$.data.generation.resetTimeSeconds").value(12345))
                .andExpect(jsonPath("$.data.interactive.limit").value(20))
                .andExpect(jsonPath("$.data.interactive.used").value(5))
                .andExpect(jsonPath("$.data.interactive.remaining").value(15))
                .andExpect(jsonPath("$.data.interactive.resetTimeSeconds").value(54321));
    }
}

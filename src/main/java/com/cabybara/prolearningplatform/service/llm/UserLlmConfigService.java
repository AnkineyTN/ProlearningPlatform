package com.cabybara.prolearningplatform.service.llm;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.request.llm.SaveLlmConfigRequestDto;
import com.cabybara.prolearningplatform.dto.request.llm.TestLlmConnectionRequestDto;
import com.cabybara.prolearningplatform.dto.response.llm.LlmConfigResponseDto;
import com.cabybara.prolearningplatform.dto.response.llm.TestLlmConnectionResponseDto;

import java.util.List;

public interface UserLlmConfigService {

    // ---- CRUD for the current user (API-key always masked in responses) ----
    List<LlmConfigResponseDto> getMyConfigs();

    LlmConfigResponseDto getMyConfig(Long id);

    LlmConfigResponseDto createConfig(SaveLlmConfigRequestDto request);

    LlmConfigResponseDto updateConfig(Long id, SaveLlmConfigRequestDto request);

    void deleteConfig(Long id);

    LlmConfigResponseDto setActive(Long id);

    /**
     * Verifies a provider/model/API key against the AI Service without persisting it.
     * Always returns a result (check {@code valid}); a thrown exception means the check itself
     * could not be performed (e.g. unsupported provider, AI Service unreachable).
     */
    TestLlmConnectionResponseDto testConnection(TestLlmConnectionRequestDto request);

    // ---- Internal: used by the AI layer to forward the user's key ----
    /**
     * Returns the user's ACTIVE decrypted config, or {@code null} if no config is active.
     * When null is passed to {@code AIServiceClient}, the AI Service falls back to its own default model.
     */
    DecryptedLlmConfig getDecryptedConfig(Long userId);

    /**
     * Convenience for request-thread calls: resolves the current authenticated user.
     * Returns {@code null} when the user has no active config (AI Service uses its default).
     */
    DecryptedLlmConfig getDecryptedConfigForCurrentUser();
}

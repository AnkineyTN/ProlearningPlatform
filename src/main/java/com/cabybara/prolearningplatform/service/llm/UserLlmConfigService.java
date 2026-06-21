package com.cabybara.prolearningplatform.service.llm;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.request.llm.SaveLlmConfigRequestDto;
import com.cabybara.prolearningplatform.dto.response.llm.LlmConfigResponseDto;

import java.util.List;

public interface UserLlmConfigService {

    // ---- CRUD for the current user (API-key always masked in responses) ----
    List<LlmConfigResponseDto> getMyConfigs();

    LlmConfigResponseDto getMyConfig(Long id);

    LlmConfigResponseDto createConfig(SaveLlmConfigRequestDto request);

    LlmConfigResponseDto updateConfig(Long id, SaveLlmConfigRequestDto request);

    void deleteConfig(Long id);

    LlmConfigResponseDto setActive(Long id);

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

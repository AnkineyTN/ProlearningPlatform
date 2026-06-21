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
    /** Returns the user's ACTIVE decrypted config, or throws LlmNotConfiguredException if none is active. */
    DecryptedLlmConfig getDecryptedConfig(Long userId);

    /** Convenience for request-thread calls: resolves the current authenticated user. */
    DecryptedLlmConfig getDecryptedConfigForCurrentUser();
}

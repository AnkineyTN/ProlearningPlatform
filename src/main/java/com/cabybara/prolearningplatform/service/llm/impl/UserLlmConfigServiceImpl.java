package com.cabybara.prolearningplatform.service.llm.impl;

import com.cabybara.prolearningplatform.dto.internal.AiTestConnectionResult;
import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.request.llm.SaveLlmConfigRequestDto;
import com.cabybara.prolearningplatform.dto.request.llm.TestLlmConnectionRequestDto;
import com.cabybara.prolearningplatform.dto.response.llm.LlmConfigResponseDto;
import com.cabybara.prolearningplatform.dto.response.llm.TestLlmConnectionResponseDto;
import com.cabybara.prolearningplatform.enums.LlmProvider;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.LlmConfigMapper;
import com.cabybara.prolearningplatform.model.llm.UserLlmConfig;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.repository.llm.UserLlmConfigRepository;
import com.cabybara.prolearningplatform.service.ai.AIServiceClient;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
import com.cabybara.prolearningplatform.utils.AesGcmEncryptor;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLlmConfigServiceImpl implements UserLlmConfigService {

    private final UserLlmConfigRepository repository;
    private final UserRepository userRepository;
    private final AesGcmEncryptor encryptor;
    private final LlmConfigMapper mapper;
    private final AuthenticationContext authenticationContext;
    private final AIServiceClient aiServiceClient;

    @Override
    @Transactional(readOnly = true)
    public List<LlmConfigResponseDto> getMyConfigs() {
        Long userId = authenticationContext.getCurrentUserId();
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LlmConfigResponseDto getMyConfig(Long id) {
        return mapper.toResponseDto(getOwnedConfig(id));
    }

    @Override
    @Transactional
    public LlmConfigResponseDto createConfig(SaveLlmConfigRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();

        LlmProvider provider = parseProvider(request.getProvider());
        String rawKey = request.getApiKey();
        if (rawKey == null || rawKey.isBlank()) {
            throw new BadRequestException("apiKey is required");
        }
        rawKey = rawKey.trim();

        boolean makeActive = Boolean.TRUE.equals(request.getSetActive()) || repository.countByUserId(userId) == 0;
        if (makeActive) {
            repository.deactivateAll(userId);
        }

        UserLlmConfig config = UserLlmConfig.builder()
                .user(userRepository.getReferenceById(userId))
                .displayName(request.getDisplayName())
                .provider(provider)
                .model(request.getModel().trim())
                .apiKeyEncrypted(encryptor.encrypt(rawKey))
                .apiKeyLast4(last4(rawKey))
                .active(makeActive)
                .build();

        return mapper.toResponseDto(repository.save(config));
    }

    @Override
    @Transactional
    public LlmConfigResponseDto updateConfig(Long id, SaveLlmConfigRequestDto request) {
        UserLlmConfig config = getOwnedConfig(id);

        config.setProvider(parseProvider(request.getProvider()));
        config.setModel(request.getModel().trim());
        if (request.getDisplayName() != null) {
            config.setDisplayName(request.getDisplayName());
        }
        // Re-encrypt only when a new key is supplied; otherwise keep the existing one.
        if (request.getApiKey() != null && !request.getApiKey().isBlank()) {
            String rawKey = request.getApiKey().trim();
            config.setApiKeyEncrypted(encryptor.encrypt(rawKey));
            config.setApiKeyLast4(last4(rawKey));
        }

        return mapper.toResponseDto(repository.save(config));
    }

    @Override
    @Transactional
    public void deleteConfig(Long id) {
        UserLlmConfig config = getOwnedConfig(id);
        // If the active config is removed, the user is left with no active config until they set one.
        repository.delete(config);
    }

    @Override
    @Transactional
    public LlmConfigResponseDto setActive(Long id) {
        Long userId = authenticationContext.getCurrentUserId();
        UserLlmConfig config = getOwnedConfig(id);
        repository.deactivateAll(userId);
        config.setActive(true);
        return mapper.toResponseDto(repository.save(config));
    }

    @Override
    @Transactional(readOnly = true)
    public DecryptedLlmConfig getDecryptedConfig(Long userId) {
        return repository.findByUserIdAndActiveTrue(userId)
                .map(config -> new DecryptedLlmConfig(
                        config.getProvider(),
                        config.getModel(),
                        encryptor.decrypt(config.getApiKeyEncrypted())))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public DecryptedLlmConfig getDecryptedConfigForCurrentUser() {
        return getDecryptedConfig(authenticationContext.getCurrentUserId());
    }

    @Override
    public TestLlmConnectionResponseDto testConnection(TestLlmConnectionRequestDto request) {
        LlmProvider provider = parseProvider(request.getProvider());
        String model = request.getModel().trim();
        String apiKey = request.getApiKey().trim();

        AiTestConnectionResult result = aiServiceClient.testConnection(provider.getWireValue(), model, apiKey);

        return TestLlmConnectionResponseDto.builder()
                .valid(result.valid())
                .provider(result.provider())
                .model(result.model())
                .message(result.message())
                .build();
    }

    // ---- helpers ----

    private UserLlmConfig getOwnedConfig(Long id) {
        Long userId = authenticationContext.getCurrentUserId();
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("LLM config not found"));
    }

    private LlmProvider parseProvider(String provider) {
        try {
            return LlmProvider.fromWireValue(provider);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private static String last4(String raw) {
        return (raw == null || raw.length() <= 4) ? "****" : raw.substring(raw.length() - 4);
    }
}

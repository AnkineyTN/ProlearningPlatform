package com.cabybara.prolearningplatform.service.llm;

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
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.llm.UserLlmConfig;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.repository.llm.UserLlmConfigRepository;
import com.cabybara.prolearningplatform.service.ai.AIServiceClient;
import com.cabybara.prolearningplatform.service.llm.impl.UserLlmConfigServiceImpl;
import com.cabybara.prolearningplatform.utils.AesGcmEncryptor;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLlmConfigServiceImplTest {

    @Mock
    private UserLlmConfigRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AesGcmEncryptor encryptor;
    @Mock
    private LlmConfigMapper mapper;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private AIServiceClient aiServiceClient;

    @InjectMocks
    private UserLlmConfigServiceImpl service;

    private SaveLlmConfigRequestDto request(String provider, String model, String apiKey) {
        SaveLlmConfigRequestDto dto = new SaveLlmConfigRequestDto();
        dto.setProvider(provider);
        dto.setModel(model);
        dto.setApiKey(apiKey);
        return dto;
    }

    private TestLlmConnectionRequestDto testConnectionRequest(String provider, String model, String apiKey) {
        TestLlmConnectionRequestDto dto = new TestLlmConnectionRequestDto();
        dto.setProvider(provider);
        dto.setModel(model);
        dto.setApiKey(apiKey);
        return dto;
    }

    @Test
    void createFirstConfigBecomesActive() {
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(repository.countByUserId(1L)).thenReturn(0L);
        when(userRepository.getReferenceById(1L)).thenReturn(mock(User.class));
        when(encryptor.encrypt("sk-abcd1234")).thenReturn("ENC");
        when(repository.save(any(UserLlmConfig.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponseDto(any())).thenReturn(LlmConfigResponseDto.builder().build());

        service.createConfig(request("openai", "gpt-4o-mini", "sk-abcd1234"));

        verify(repository).deactivateAll(1L);
        ArgumentCaptor<UserLlmConfig> captor = ArgumentCaptor.forClass(UserLlmConfig.class);
        verify(repository).save(captor.capture());
        UserLlmConfig saved = captor.getValue();
        assertTrue(saved.isActive());
        assertEquals(LlmProvider.OPENAI, saved.getProvider());
        assertEquals("ENC", saved.getApiKeyEncrypted());
        assertEquals("1234", saved.getApiKeyLast4());
    }

    @Test
    void createNonFirstConfigIsNotActiveByDefault() {
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(repository.countByUserId(1L)).thenReturn(2L);
        when(userRepository.getReferenceById(1L)).thenReturn(mock(User.class));
        when(encryptor.encrypt(any())).thenReturn("ENC");
        when(repository.save(any(UserLlmConfig.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponseDto(any())).thenReturn(LlmConfigResponseDto.builder().build());

        service.createConfig(request("anthropic", "claude", "sk-xyz9999"));

        verify(repository, never()).deactivateAll(anyLong());
        ArgumentCaptor<UserLlmConfig> captor = ArgumentCaptor.forClass(UserLlmConfig.class);
        verify(repository).save(captor.capture());
        assertFalse(captor.getValue().isActive());
    }

    @Test
    void createWithoutApiKeyThrows() {
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        assertThrows(BadRequestException.class,
                () -> service.createConfig(request("openai", "gpt", null)));
    }

    @Test
    void createWithUnsupportedProviderThrows() {
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        assertThrows(BadRequestException.class,
                () -> service.createConfig(request("not-a-provider", "gpt", "sk-1")));
    }

    @Test
    void getMyConfigForOtherUsersConfigThrowsNotFound() {
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(repository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getMyConfig(99L));
    }

    @Test
    void setActiveDeactivatesOthersAndActivatesTarget() {
        UserLlmConfig config = UserLlmConfig.builder()
                .provider(LlmProvider.OPENAI).model("gpt").apiKeyEncrypted("ENC").active(false).build();
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(repository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(config));
        when(repository.save(config)).thenReturn(config);
        when(mapper.toResponseDto(config)).thenReturn(LlmConfigResponseDto.builder().build());

        service.setActive(5L);

        verify(repository).deactivateAll(1L);
        assertTrue(config.isActive());
    }

    @Test
    void deleteConfigRemovesOwnedConfig() {
        UserLlmConfig config = UserLlmConfig.builder().provider(LlmProvider.OPENAI).model("gpt").build();
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(repository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(config));

        service.deleteConfig(5L);

        verify(repository).delete(config);
    }

    @Test
    void getDecryptedConfigReturnsActiveDecrypted() {
        UserLlmConfig config = UserLlmConfig.builder()
                .provider(LlmProvider.ANTHROPIC).model("claude-3-5").apiKeyEncrypted("ENC").active(true).build();
        when(repository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(config));
        when(encryptor.decrypt("ENC")).thenReturn("sk-real-key");

        DecryptedLlmConfig result = service.getDecryptedConfig(1L);

        assertEquals(LlmProvider.ANTHROPIC, result.provider());
        assertEquals("claude-3-5", result.model());
        assertEquals("sk-real-key", result.apiKey());
    }

    @Test
    void getDecryptedConfigWithNoActiveConfigReturnsNull() {
        when(repository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.empty());
        assertNull(service.getDecryptedConfig(1L));
    }

    @Test
    void testConnectionDelegatesToAiServiceClientAndMapsResult() {
        when(aiServiceClient.testConnection("openai", "gpt-4o-mini", "sk-test"))
                .thenReturn(new AiTestConnectionResult(true, "openai", "gpt-4o-mini", "Connection successful"));

        TestLlmConnectionResponseDto result = service.testConnection(
                testConnectionRequest("openai", "gpt-4o-mini", "sk-test"));

        assertTrue(result.isValid());
        assertEquals("openai", result.getProvider());
        assertEquals("gpt-4o-mini", result.getModel());
        assertEquals("Connection successful", result.getMessage());
    }

    @Test
    void testConnectionWithInvalidKeyStillReturnsResultInsteadOfThrowing() {
        when(aiServiceClient.testConnection("openai", "gpt-4o-mini", "sk-bad"))
                .thenReturn(new AiTestConnectionResult(false, "openai", "gpt-4o-mini", "Incorrect API key provided"));

        TestLlmConnectionResponseDto result = service.testConnection(
                testConnectionRequest("openai", "gpt-4o-mini", "sk-bad"));

        assertFalse(result.isValid());
        assertEquals("Incorrect API key provided", result.getMessage());
    }

    @Test
    void testConnectionWithUnsupportedProviderThrowsWithoutCallingAiService() {
        assertThrows(BadRequestException.class,
                () -> service.testConnection(testConnectionRequest("not-a-provider", "gpt", "sk-1")));
        verifyNoInteractions(aiServiceClient);
    }
}

package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.request.flashcard.AIGenerateFlashcardByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByFileRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.enums.LlmProvider;
import com.cabybara.prolearningplatform.service.ai.impl.AIFlashcardServiceImpl;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIFlashcardServiceImplTest {

    private static final DecryptedLlmConfig CFG = new DecryptedLlmConfig(LlmProvider.OPENAI, "gpt-4o-mini", "sk-test");

    @Mock
    private AIServiceClient aiServiceClient;

    @Mock
    private UserLlmConfigService userLlmConfigService;

    @Test
    void generateFlashcardByNotesReturnsCards() {
        AIFlashcardServiceImpl service = new AIFlashcardServiceImpl(aiServiceClient, new ObjectMapper(), userLlmConfigService);

        AIGenerateFlashcardByNoteRequestDto request = AIGenerateFlashcardByNoteRequestDto.builder()
                .notes(List.of())
                .freeText("test topic")
                .build();

        when(userLlmConfigService.getDecryptedConfigForCurrentUser()).thenReturn(CFG);
        when(aiServiceClient.postForGeneration(anyString(), any(), any(), eq(String.class)))
                .thenReturn("{\"title\":\"Test Title\",\"description\":\"Test Description\",\"data\":\"card data\"}");

        GenerateFlashcardByAIResponseDto response = service.generateFlashcardByNotes(request);

        assertNotNull(response);
        assertNotNull(response.getContent());
    }

    @Test
    void generateFlashcardByFileReturnsCards() {
        AIFlashcardServiceImpl service = new AIFlashcardServiceImpl(aiServiceClient, new ObjectMapper(), userLlmConfigService);

        GenerateFlashcardByFileRequestDto request = GenerateFlashcardByFileRequestDto.builder()
                .files(List.of())
                .freeText("test topic")
                .build();

        when(userLlmConfigService.getDecryptedConfigForCurrentUser()).thenReturn(CFG);
        when(aiServiceClient.postMultipartForGeneration(anyString(), any(), any(), eq(String.class)))
                .thenReturn("{\"title\":\"Test Title\",\"description\":\"Test Description\",\"data\":\"card data\"}");

        GenerateFlashcardByAIResponseDto response = service.generateFlashcardByFiles(request);

        assertNotNull(response);
    }
}

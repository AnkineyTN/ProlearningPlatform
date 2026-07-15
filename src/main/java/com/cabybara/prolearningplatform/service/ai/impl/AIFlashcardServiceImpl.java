package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.request.flashcard.AIGenerateFlashcardByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByWebRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.exception.AIServiceException;
import com.cabybara.prolearningplatform.exception.LlmNotConfiguredException;
import com.cabybara.prolearningplatform.service.ai.AIFlashcardService;
import com.cabybara.prolearningplatform.service.ai.AIServiceClient;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIFlashcardServiceImpl implements AIFlashcardService {
    // =============================================
    // ==== PREPARATION
    // =============================================
    private static final String GENERATE_FLASHCARD_BY_FILE_PATH = "/flashcards/from-file";
    private static final String GENERATE_FLASHCARD_BY_NOTE_PATH = "/flashcards/from-note";
    private static final String GENERATE_FLASHCARD_BY_WEB_PATH = "/flashcards/from-web";

    private final AIServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;
    private final UserLlmConfigService userLlmConfigService;

    private GenerateFlashcardByAIResponseDto parseResponse(String json) {
        try {
            var root = objectMapper.readTree(json);
            return GenerateFlashcardByAIResponseDto.builder()
                    .title(root.path("title").asText(""))
                    .description(root.path("description").asText(""))
                    .content(root.path("data").asText(""))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI service response", e);
        }
    }

    private Resource convertToResource(MultipartFile file) throws IOException {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }

    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcardByFiles(GenerateFlashcardByFileRequestDto request) {
        DecryptedLlmConfig cfg = userLlmConfigService.getDecryptedConfigForCurrentUser();
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            for (MultipartFile file : request.getFiles()) {
                body.add("files", convertToResource(file));
            }
            body.add("free_text", request.getFreeText());
            body.add("language", request.getLanguage());

            String raw = aiServiceClient.postMultipartForGeneration(
                    GENERATE_FLASHCARD_BY_FILE_PATH, body, cfg, String.class);

            return parseResponse(raw);

        } catch (AIServiceException | LlmNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for generating Flashcard by Files", e);
        }
    }

    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcardByNotes(AIGenerateFlashcardByNoteRequestDto request) {
        DecryptedLlmConfig cfg = userLlmConfigService.getDecryptedConfigForCurrentUser();
        try {
            Map<String, Object> body = objectMapper.convertValue(request, Map.class);

            String raw = aiServiceClient.postForGeneration(
                    GENERATE_FLASHCARD_BY_NOTE_PATH, body, cfg, String.class);

            return parseResponse(raw);

        } catch (AIServiceException | LlmNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for generating Flashcard by Notes", e);
        }
    }

    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcardByWeb(GenerateFlashcardByWebRequestDto request) {
        DecryptedLlmConfig cfg = userLlmConfigService.getDecryptedConfigForCurrentUser();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("urls", request.getUrls());
            body.put("free_text", request.getFreeText() != null ? request.getFreeText() : "");
            body.put("language", request.getLanguage() != null ? request.getLanguage() : "English");

            String raw = aiServiceClient.postForGeneration(
                    GENERATE_FLASHCARD_BY_WEB_PATH, body, cfg, String.class);

            return parseResponse(raw);

        } catch (AIServiceException | LlmNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for generating Flashcard by Web", e);
        }
    }
}

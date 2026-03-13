package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.note.ConvertFileToVectorRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.ExplainNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.SummarizeFileRequestDTO;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.note.ExplainNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.SummarizeFileResponseDTO;
import com.cabybara.prolearningplatform.service.ai.AIFlashcardService;
import com.cabybara.prolearningplatform.service.ai.AINoteService;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIFlashcardServiceImpl implements AIFlashcardService {
    // =============================================
    // ==== PREPARATION
    // =============================================
    @Value("${aiservice.api}")
    private String aiServiceBaseApi;

    private static final String GENERATE_FLASHCARD_BY_FILE_PATH = "/flashcards/from-file";
    private static final String GENERATE_FLASHCARD_BY_NOTE_PATH = "/flashcards/from-note";

    private final RestHttpClientUtil restHttpClientUtil;
    private final ObjectMapper objectMapper;

    private String parseData(String json) {
        try {
            return objectMapper.readTree(json).path("data").asText("");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI service response", e);
        }
    }

    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcard(String content, String type) {
        try {
            String path = switch (type) {
                case "file" -> GENERATE_FLASHCARD_BY_FILE_PATH;
                case "note" -> GENERATE_FLASHCARD_BY_NOTE_PATH;
                default -> throw new IllegalArgumentException("Invalid type: " + type);
            };

            String raw = restHttpClientUtil.post(
                    aiServiceBaseApi + path,
                    Map.of("content", content),
                    String.class
            );

            return GenerateFlashcardByAIResponseDto.builder()
                    .content(parseData(raw))
                    .build();

        } catch (Exception e) {
            log.error("❌ Error generating flashcard with AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate flashcard with AI", e);
        }
    }
}

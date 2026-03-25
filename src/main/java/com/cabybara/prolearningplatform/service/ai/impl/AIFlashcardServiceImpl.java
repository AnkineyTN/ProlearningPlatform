package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByWebRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.enums.Language;
import com.cabybara.prolearningplatform.service.ai.AIFlashcardService;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

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
    private static final String GENERATE_FLASHCARD_BY_WEB_PATH = "/flashcards/from-web";

    private final RestHttpClientUtil restHttpClientUtil;
    private final ObjectMapper objectMapper;

    private String parseData(String json) {
        try {
            return objectMapper.readTree(json).path("data").asText("");
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
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Add files
            for (MultipartFile file : request.getFiles()) {
                body.add("files", convertToResource(file));
            }

            // Add extra fields
            body.add("freeText", request.getFreeText());
            body.add("language", request.getLanguage());

            String raw = restHttpClientUtil.postMultipart(
                    aiServiceBaseApi + GENERATE_FLASHCARD_BY_FILE_PATH,
                    body,
                    String.class
            );

            return GenerateFlashcardByAIResponseDto.builder()
                    .content(parseData(raw))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for generating Flashcard by Files", e);
        }
    }

    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcardByNotes(List<String> contents, String freeText, Language language
    ) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("contents", contents);
            body.put("free_text", freeText != null ? freeText : "");
            body.put("language", language != null ? language : "English");

            String raw = restHttpClientUtil.post(
                    aiServiceBaseApi + GENERATE_FLASHCARD_BY_NOTE_PATH,
                    body,
                    String.class
            );

            return GenerateFlashcardByAIResponseDto.builder()
                    .content(parseData(raw))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for generating Flashcard by Notes", e);
        }
    }

    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcardByWeb(GenerateFlashcardByWebRequestDto request) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("urls", request.getUrls());
            body.put("free_text", request.getFreeText() != null ? request.getFreeText() : "");
            body.put("language", request.getLanguage() != null ? request.getLanguage() : "English");

            String raw = restHttpClientUtil.post(
                    aiServiceBaseApi + GENERATE_FLASHCARD_BY_WEB_PATH,
                    body,
                    String.class
            );

            return GenerateFlashcardByAIResponseDto.builder()
                    .content(parseData(raw))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for generating Flashcard by Web", e);
        }
    }
}

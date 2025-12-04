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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AIFlashcardServiceImpl implements AIFlashcardService {
    private static final String GENERATE_FLASHCARD_BY_FILES = "https://prolearning-aiservice.onrender.com/flashcard/generate-by-file";

    private static final String GENERATE_FLASHCARD_BY_NOTES = "https://prolearning-aiservice.onrender.com/generate-by-note";


    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcard(String content, String type) {
        try {
            // Create RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Put headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> request = new HashMap<>();
            request.put("content", content);

            // Create request body
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // Call API
            ResponseEntity<String> response = null;
            if (type.equals("file")) {
                response = restTemplate.exchange(
                        GENERATE_FLASHCARD_BY_FILES,
                        HttpMethod.POST,
                        entity,
                        String.class
                );
                log.info("🐳 Response from AI Service while generating flashcard by file with AI: {}", response.getBody());
            } else if (type.equals("note")) {
                response = restTemplate.exchange(
                        GENERATE_FLASHCARD_BY_NOTES,
                        HttpMethod.POST,
                        entity,
                        String.class
                );
                log.info("🐳 Response from AI Service while generating flashcard by note with AI: {}", response.getBody());
            }

            if (response == null || response.getBody() == null) {
                throw new RuntimeException("No response from AI Service");
            }

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            boolean success = root.path("success").asBoolean(false);
            String message = root.path("message").asText("");
            String data = root.path("data").asText("");

            return GenerateFlashcardByAIResponseDto.builder()
                    .content(data)
                    .build();
        } catch (Exception e) {
            log.error("😡 Error generating flashcard with AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate flashcard with AI", e);
        }
    }
}

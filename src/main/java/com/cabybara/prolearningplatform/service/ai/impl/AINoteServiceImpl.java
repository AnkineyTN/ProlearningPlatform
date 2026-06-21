package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.request.note.ConvertFileToVectorRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.ExplainNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.GenerateNoteWithAIRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.SummarizeFileRequestDTO;
import com.cabybara.prolearningplatform.dto.response.note.ExplainNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GenerateNoteWithAIResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.SummarizeFileResponseDTO;
import com.cabybara.prolearningplatform.exception.AIServiceException;
import com.cabybara.prolearningplatform.exception.LlmNotConfiguredException;
import com.cabybara.prolearningplatform.service.ai.AINoteService;
import com.cabybara.prolearningplatform.service.ai.AIServiceClient;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AINoteServiceImpl implements AINoteService {
    // =============================================
    // ==== PREPARATION
    // =============================================
    private static final String CONVERT_FILE_TO_VECTOR_PATH = "/files/process";
    private static final String EXPLAIN_NOTE_PATH           = "/notes/explain";
    private static final String SUMMARY_FILE_PATH           = "/notes/summarize";
    private static final String GENERATE_NOTE_PATH          = "/notes/generate";

    private final AIServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;
    private final UserLlmConfigService userLlmConfigService;

    // =============================================
    // ==== UTILS
    // =============================================
    private JsonNode parseDataNode(String json) {
        try {
            return objectMapper.readTree(json).path("data");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI service response", e);
        }
    }

    private String parseData(String json) {
        return parseDataNode(json).asText("");
    }

    // =============================================
    // ==== METHODS
    // =============================================
    @Override
    public void convertFileToVector(ConvertFileToVectorRequestDTO request) {
        // Embeddings / vector DB use the AI Service's own key — no per-user LLM headers.
        try {
            aiServiceClient.postInternalOnly(CONVERT_FILE_TO_VECTOR_PATH, request, String.class);
        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Error converting file to vector db: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert file to vector db", e);
        }
    }

    @Override
    public ExplainNoteResponseDTO explainNote(ExplainNoteRequestDTO request) {
        DecryptedLlmConfig cfg = userLlmConfigService.getDecryptedConfigForCurrentUser();
        try {
            String raw = aiServiceClient.postForGeneration(EXPLAIN_NOTE_PATH, request, cfg, String.class);
            return ExplainNoteResponseDTO.builder()
                    .queryText(request.getQueryText())
                    .answer(parseData(raw))
                    .build();
        } catch (AIServiceException | LlmNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Error explaining note with AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to explain note with AI", e);
        }
    }

    @Override
    public SummarizeFileResponseDTO summarizeFile(SummarizeFileRequestDTO request) {
        DecryptedLlmConfig cfg = userLlmConfigService.getDecryptedConfigForCurrentUser();
        try {
            String raw = aiServiceClient.postForGeneration(SUMMARY_FILE_PATH, request, cfg, String.class);
            return SummarizeFileResponseDTO.builder()
                    .summary(parseData(raw))
                    .build();
        } catch (AIServiceException | LlmNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Error summarizing file with AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to summarize file with AI", e);
        }
    }

    @Override
    public GenerateNoteWithAIResponseDTO generateNoteContent(GenerateNoteWithAIRequestDTO request) {
        DecryptedLlmConfig cfg = userLlmConfigService.getDecryptedConfigForCurrentUser();
        try {
            String raw = aiServiceClient.postForGeneration(GENERATE_NOTE_PATH, request, cfg, String.class);
            return objectMapper.treeToValue(parseDataNode(raw), GenerateNoteWithAIResponseDTO.class);
        } catch (AIServiceException | LlmNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Error generating note with AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate note content with AI", e);
        }
    }
}

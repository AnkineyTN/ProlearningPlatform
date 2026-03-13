package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.request.note.ConvertFileToVectorRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.ExplainNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.SummarizeFileRequestDTO;
import com.cabybara.prolearningplatform.dto.response.note.ExplainNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.SummarizeFileResponseDTO;
import com.cabybara.prolearningplatform.service.ai.AINoteService;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AINoteServiceImpl implements AINoteService {
    // =============================================
    // ==== PREPARATION
    // =============================================
    @Value("${aiservice.api}")
    private String aiServiceBaseApi;

    private static final String CONVERT_FILE_TO_VECTOR_PATH = "/files/process";
    private static final String EXPLAIN_NOTE_PATH           = "/notes/explain";
    private static final String SUMMARY_FILE_PATH           = "/notes/summarize";

    private final RestHttpClientUtil restHttpClientUtil;
    private final ObjectMapper objectMapper;

    // =============================================
    // ==== UTILS
    // =============================================
    private String parseData(String json) {
        try {
            return objectMapper.readTree(json).path("data").asText("");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI service response", e);
        }
    }

    // =============================================
    // ==== METHODS
    // =============================================
    @Override
    public void convertFileToVector(ConvertFileToVectorRequestDTO request) {
        try {
            restHttpClientUtil.post(aiServiceBaseApi + CONVERT_FILE_TO_VECTOR_PATH, request, String.class);
        } catch (Exception e) {
            log.error("❌ Error converting file to vector db: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert file to vector db", e);
        }
    }

    @Override
    public ExplainNoteResponseDTO explainNote(ExplainNoteRequestDTO request) {
        try {
            String raw = restHttpClientUtil.post(aiServiceBaseApi + EXPLAIN_NOTE_PATH, request, String.class);
            return ExplainNoteResponseDTO.builder()
                    .queryText(request.getQueryText())
                    .answer(parseData(raw))
                    .build();
        } catch (Exception e) {
            log.error("❌ Error explaining note with AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to explain note with AI", e);
        }
    }

    @Override
    public SummarizeFileResponseDTO summarizeFile(SummarizeFileRequestDTO request) {
        try {
            String raw = restHttpClientUtil.post(aiServiceBaseApi + SUMMARY_FILE_PATH, request, String.class);
            return SummarizeFileResponseDTO.builder()
                    .summary(parseData(raw))
                    .build();
        } catch (Exception e) {
            log.error("❌ Error explaining note with AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to summarize file with AI", e);
        }
    }
}

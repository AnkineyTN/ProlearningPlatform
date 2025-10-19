package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.request.ConvertFileToVectorRequestDTO;
import com.cabybara.prolearningplatform.dto.request.ExplainNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.SummarizeFileRequestDTO;
import com.cabybara.prolearningplatform.dto.response.ExplainNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.SummarizeFileResponseDTO;
import com.cabybara.prolearningplatform.service.ai.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
@Slf4j
public class AIServiceImpl implements AIService {
    private static final String CONVERT_FILE_TO_VECTOR = "https://prolearning-aiservice.onrender.com/files-loader/all";
    private static final String EXPLAIN_NOTE = "https://prolearning-aiservice.onrender.com/note/explain";
    private static final String SUMMARY_FILE = "https://prolearning-aiservice.onrender.com/note/summarize";

    @Override
    public void convertFileToVector(ConvertFileToVectorRequestDTO request) {
        try {
            // Create RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Put headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request body
            HttpEntity<ConvertFileToVectorRequestDTO> entity = new HttpEntity<>(request, headers);

            // Call API
            ResponseEntity<String> response = restTemplate.exchange(
                    CONVERT_FILE_TO_VECTOR,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            log.info("Response from AI Service: {}", response.getBody());

        } catch (Exception e) {
            log.error("❌ Error converting file to vector db: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert file to vector db", e);
        }
    }

    @Override
    public ExplainNoteResponseDTO explainNote(ExplainNoteRequestDTO request) {
        try {
            // Create RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Put headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request body
            HttpEntity<ExplainNoteRequestDTO> entity = new HttpEntity<>(request, headers);

            // Call API
            ResponseEntity<String> response = restTemplate.exchange(
                    EXPLAIN_NOTE,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            log.info("Response from AI Service: {}", response.getBody());

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            boolean success = root.path("success").asBoolean(false);
            String message = root.path("message").asText("");
            String data = root.path("data").asText("");

            return ExplainNoteResponseDTO.builder()
                    .queryText(request.getQueryText())
                    .answer(data)
                    .build();
        } catch (Exception e) {
            log.error("❌ Error explaining note with AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to explain note with AI", e);
        }
    }

    @Override
    public SummarizeFileResponseDTO summarizeFile(SummarizeFileRequestDTO request) {
        try {
            // Create RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Put headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request body
            HttpEntity<SummarizeFileRequestDTO> entity = new HttpEntity<>(request, headers);

            // Call API
            ResponseEntity<String> response = restTemplate.exchange(
                    SUMMARY_FILE,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            log.info("Response from AI Service: {}", response.getBody());

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            boolean success = root.path("success").asBoolean(false);
            String message = root.path("message").asText("");
            String data = root.path("data").asText("");

            return SummarizeFileResponseDTO.builder()
                    .noteDocsId(request.getNoteDocsId())
                    .summary(data)
                    .build();
        } catch (Exception e) {
            log.error("❌ Error explaining note with AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to explain note with AI", e);
        }
    }
}

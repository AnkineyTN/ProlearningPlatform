package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.service.ai.AIExamService;
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
public class AIExamServiceImpl implements AIExamService {
    private static final String GENERATE_EXAM_BY_FILES = "https://prolearning-ai-service-dev.onrender.com/api/v1/tests/from-file";
    private static final String GENERATE_EXAM_BY_NOTES = "https://prolearning-ai-service-dev.onrender.com/api/v1/tests/from-note";

    @Override
    public GenerateExamByAIResponseDto generateExam(Map<String, Object> requestBody) {
        try {
            String type = (String) requestBody.get("type");
            requestBody.remove("type");

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = "note".equals(type) ? GENERATE_EXAM_BY_NOTES : GENERATE_EXAM_BY_FILES;
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            log.info("🐳 Response from AI Service (type={}): {}", type, response.getBody());

            if (response.getBody() == null) {
                throw new RuntimeException("No response from AI Service");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            String result = root.path("result").asText("");

            return GenerateExamByAIResponseDto.builder()
                    .content(result)
                    .build();

        } catch (Exception e) {
            log.error("😡 Error generating exam with AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate exam with AI", e);
        }
    }
}

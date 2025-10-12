package com.cabybara.prolearningplatform.service.impl;

import com.cabybara.prolearningplatform.dto.request.ConvertFileToVectorRequestDTO;
import com.cabybara.prolearningplatform.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
@Slf4j
public class AIServiceImpl implements AIService {
    private static final String NODE_API_URL = "http://localhost:3333/files-loader/all";

    @Override
    public void convertFileToVector(ConvertFileToVectorRequestDTO request) {
        try {
            // Tạo RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Đặt headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Tạo request body
            HttpEntity<ConvertFileToVectorRequestDTO> entity = new HttpEntity<>(request, headers);

            // Gọi API Node.js
            ResponseEntity<String> response = restTemplate.exchange(
                    NODE_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            log.info("Response from Node.js: {}", response.getBody());

        } catch (Exception e) {
            log.error("❌ Error calling Node.js API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Node.js service", e);
        }
    }
}

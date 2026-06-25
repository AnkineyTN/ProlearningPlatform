package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.internal.knowledge.KnowledgeAIResult;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentItem;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentResult;
import com.cabybara.prolearningplatform.dto.response.knowledge.TopicAccuracyDto;
import com.cabybara.prolearningplatform.exception.AIServiceException;
import com.cabybara.prolearningplatform.exception.LlmNotConfiguredException;
import com.cabybara.prolearningplatform.service.ai.AIKnowledgeService;
import com.cabybara.prolearningplatform.service.ai.AIServiceClient;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIKnowledgeServiceImpl implements AIKnowledgeService {

    private static final String ASSIGN_TOPIC_PATH = "/analyze/assign-topic";
    private static final String NORMALIZE_TOPIC_PATH = "/analyze/normalize-topic";
    private static final String ANALYZE_PERFORMANCE_PATH = "/analyze/analyze-performance";

    private final AIServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;
    private final UserLlmConfigService userLlmConfigService;

    @Override
    public List<TopicAssignmentResult> assignTopics(List<TopicAssignmentItem> items) {
        return assignTopics(items, userLlmConfigService.getDecryptedConfigForCurrentUser());
    }

    @Override
    public List<TopicAssignmentResult> assignTopics(List<TopicAssignmentItem> items, DecryptedLlmConfig cfg) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("items", items);

            String raw = aiServiceClient.postForGeneration(ASSIGN_TOPIC_PATH, body, cfg, String.class);

            JsonNode root = objectMapper.readTree(raw);
            return objectMapper.readValue(
                    root.path("data").toString(),
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, TopicAssignmentResult.class)
            );
        } catch (AIServiceException | LlmNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for topic assignment", e);
        }
    }

    @Override
    public Map<String, String> normalizeTopics(List<String> topics) {
        DecryptedLlmConfig cfg = userLlmConfigService.getDecryptedConfigForCurrentUser();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("topics", topics);

            String raw = aiServiceClient.postForGeneration(NORMALIZE_TOPIC_PATH, body, cfg, String.class);

            JsonNode root = objectMapper.readTree(raw);
            return objectMapper.readValue(
                    root.path("data").toString(),
                    new TypeReference<Map<String, String>>() {
                    }
            );
        } catch (AIServiceException | LlmNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for topic normalization", e);
        }
    }

    @Override
    public KnowledgeAIResult analyzeKnowledge(List<TopicAccuracyDto> topicAccuracies) {
        DecryptedLlmConfig cfg = userLlmConfigService.getDecryptedConfigForCurrentUser();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("topic_accuracies", topicAccuracies);

            String raw = aiServiceClient.postForGeneration(ANALYZE_PERFORMANCE_PATH, body, cfg, String.class);

            JsonNode data = objectMapper
                    .readTree(raw)
                    .path("data");

            return new KnowledgeAIResult(
                    data.path("strengths").asText(),
                    data.path("weaknesses").asText(),
                    data.path("improvements").asText()
            );
        } catch (AIServiceException | LlmNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for knowledge analysis", e);
        }
    }
}

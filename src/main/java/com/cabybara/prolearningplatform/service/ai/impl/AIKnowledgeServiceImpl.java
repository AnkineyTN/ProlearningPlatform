package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.internal.knowledge.KnowledgeAIResult;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentItem;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentResult;
import com.cabybara.prolearningplatform.dto.response.knowledge.TopicAccuracyDto;
import com.cabybara.prolearningplatform.service.ai.AIKnowledgeService;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIKnowledgeServiceImpl implements AIKnowledgeService {

    @Value("${aiservice.api}")
    private String aiServiceBaseApi;

    private static final String ASSIGN_TOPIC_PATH = "/analyze/assign-topic";
    private static final String NORMALIZE_TOPIC_PATH = "/analyze/normalize-topic";
    private static final String ANALYZE_PERFORMANCE_PATH = "/analyze/analyze-performance";

    private final RestHttpClientUtil restHttpClientUtil;
    private final ObjectMapper objectMapper;

    @Override
    public List<TopicAssignmentResult> assignTopics(List<TopicAssignmentItem> items) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("items", items);

            String raw = restHttpClientUtil.post(aiServiceBaseApi + ASSIGN_TOPIC_PATH, body, String.class);

            JsonNode root = objectMapper.readTree(raw);
            return objectMapper.readValue(
                    root.path("data").toString(),
                    objectMapper.getTypeFactory()
                            .constructCollectionType(
                                    List.class,
                                    TopicAssignmentResult.class
                            )
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for topic assignment", e);
        }
    }

    @Override
    public Map<String, String> normalizeTopics(List<String> topics) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("topics", topics);

            String raw = restHttpClientUtil.post(aiServiceBaseApi + NORMALIZE_TOPIC_PATH, body, String.class);

            JsonNode root = objectMapper.readTree(raw);
            return objectMapper.readValue(
                    root.path("data").toString(),
                    new TypeReference<Map<String, String>>() {
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for topic normalization", e);
        }
    }

    @Override
    public KnowledgeAIResult analyzeKnowledge(List<TopicAccuracyDto> topicAccuracies) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("topic_accuracies", topicAccuracies);

            String raw = restHttpClientUtil.post(aiServiceBaseApi + ANALYZE_PERFORMANCE_PATH, body, String.class);

            JsonNode data = objectMapper
                    .readTree(raw)
                    .path("data");

            return new KnowledgeAIResult(
                    data.path("strengths").asText(),
                    data.path("weaknesses").asText(),
                    data.path("improvements").asText()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for knowledge analysis", e);
        }
    }
}

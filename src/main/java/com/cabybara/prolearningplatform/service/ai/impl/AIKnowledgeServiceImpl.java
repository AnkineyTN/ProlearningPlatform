package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.internal.knowledge.KnowledgeAIResult;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentItem;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentResult;
import com.cabybara.prolearningplatform.dto.response.knowledge.TopicAccuracyDto;
import com.cabybara.prolearningplatform.service.ai.AIKnowledgeService;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
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

    private static final String ASSIGN_TOPICS_PATH    = "/knowledge/assign-topics";
    private static final String NORMALIZE_TOPICS_PATH = "/knowledge/normalize-topics";
    private static final String ANALYZE_PATH          = "/knowledge/analyze";

    private final RestHttpClientUtil restHttpClientUtil;
    private final ObjectMapper objectMapper;

    @Override
    public List<TopicAssignmentResult> assignTopics(List<TopicAssignmentItem> items) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("items", items);

            String raw = restHttpClientUtil.post(aiServiceBaseApi + ASSIGN_TOPICS_PATH, body, String.class);

            return objectMapper.readValue(raw,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TopicAssignmentResult.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for topic assignment", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> normalizeTopics(List<String> topics) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("topics", topics);

            String raw = restHttpClientUtil.post(aiServiceBaseApi + NORMALIZE_TOPICS_PATH, body, String.class);

            Map<String, Object> response = objectMapper.readValue(raw, Map.class);
            Map<String, String> mapping = new HashMap<>();
            ((Map<String, String>) response.get("mapping")).forEach(mapping::put);
            return mapping;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for topic normalization", e);
        }
    }

    @Override
    public KnowledgeAIResult analyzeKnowledge(List<TopicAccuracyDto> topicAccuracies) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("topic_accuracies", topicAccuracies);

            String raw = restHttpClientUtil.post(aiServiceBaseApi + ANALYZE_PATH, body, String.class);

            Map<?, ?> response = objectMapper.readValue(raw, Map.class);
            return new KnowledgeAIResult(
                    (String) response.get("strengths"),
                    (String) response.get("weaknesses"),
                    (String) response.get("improvements")
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for knowledge analysis", e);
        }
    }
}

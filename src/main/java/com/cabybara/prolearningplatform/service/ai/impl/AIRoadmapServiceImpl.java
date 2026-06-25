package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiRequestDto;
import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiResponseDto;
import com.cabybara.prolearningplatform.dto.internal.roadmap.UserKnowledgeProfileDto;
import com.cabybara.prolearningplatform.dto.request.roadmap.RoadmapPreviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapPreviewResponseDto;
import com.cabybara.prolearningplatform.exception.AIServiceException;
import com.cabybara.prolearningplatform.exception.LlmNotConfiguredException;
import com.cabybara.prolearningplatform.service.ai.AIRoadmapService;
import com.cabybara.prolearningplatform.service.ai.AIServiceClient;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
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
public class AIRoadmapServiceImpl implements AIRoadmapService {

    // ##################################################
    // #################  PREPARATION  ##################
    // ##################################################

    private static final String GENERATE_ROADMAP_PATH = "/roadmap/generate";
    private static final String GENERATE_TOPIC_CONTENT_PATH = "/roadmap/generate-topic-content";

    private final AIServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;
    private final UserLlmConfigService userLlmConfigService;

    // ##################################################
    // #################  MAIN METHOD  ##################
    // ##################################################

    @Override
    public RoadmapPreviewResponseDto generateRoadmap(RoadmapPreviewRequestDto request, List<UserKnowledgeProfileDto> knowledgeProfiles) {
        log.info("Generating roadmap for goal='{}', level='{}'", request.getGoal(), request.getLevel());

        DecryptedLlmConfig cfg = userLlmConfigService.getDecryptedConfigForCurrentUser();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("goal", request.getGoal());
            body.put("level", request.getLevel().getDescription());
            body.put("language", request.getLanguage() != null ? request.getLanguage() : "English");
            body.put("reference_links",
                    request.getReferenceLinks() != null ? request.getReferenceLinks() : List.of());
            body.put("knowledge_profiles", knowledgeProfiles);

            String raw = aiServiceClient.postForGeneration(GENERATE_ROADMAP_PATH, body, cfg, String.class);

            Map<String, Object> responseData = objectMapper.readValue(raw, Map.class);

            return objectMapper.convertValue(responseData, RoadmapPreviewResponseDto.class);

        } catch (AIServiceException | LlmNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call AI service for generating roadmap", e);
            throw new RuntimeException("Failed to call AI service for generating roadmap", e);
        }
    }

    @Override
    public TopicContentAiResponseDto generateTopicContent(TopicContentAiRequestDto request, DecryptedLlmConfig cfg) {
        log.info("Generating topic content for '{}' with {} previous summaries",
                request.getTopicTitle(),
                request.getPreviousSummaries() == null ? 0 : request.getPreviousSummaries().size());

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("topic_title", request.getTopicTitle());
            body.put("description", request.getDescription());
            body.put("chapter_title", request.getChapterTitle());
            body.put("chapter_objective", request.getChapterObjective());
            body.put("roadmap_title", request.getRoadmapTitle());
            body.put("previous_summaries", request.getPreviousSummaries());

            String raw = aiServiceClient.postForGeneration(GENERATE_TOPIC_CONTENT_PATH, body, cfg, String.class);

            Map<String, Object> responseData = objectMapper.readValue(raw, Map.class);

            return objectMapper.convertValue(responseData, TopicContentAiResponseDto.class);

        } catch (AIServiceException | LlmNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call AI service for generating topic content", e);
            throw new RuntimeException("Failed to call AI service for generating topic content", e);
        }
    }
}

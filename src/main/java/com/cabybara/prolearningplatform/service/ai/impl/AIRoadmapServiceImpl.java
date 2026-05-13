package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiRequestDto;
import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiResponseDto;
import com.cabybara.prolearningplatform.dto.request.roadmap.RoadmapPreviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapPreviewResponseDto;
import com.cabybara.prolearningplatform.service.ai.AIRoadmapService;
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
public class AIRoadmapServiceImpl implements AIRoadmapService {

    // ##################################################
    // #################  PREPARATION  ##################
    // ##################################################

    @Value("${aiservice.api}")
    private String aiServiceBaseApi;

    private static final String GENERATE_ROADMAP_PATH = "/roadmap/generate";
    private static final String GENERATE_TOPIC_CONTENT_PATH = "/roadmap/generate-topic-content";

    private final RestHttpClientUtil restHttpClientUtil;
    private final ObjectMapper objectMapper;

    // ##################################################
    // #################  MAIN METHOD  ##################
    // ##################################################

    @Override
    public RoadmapPreviewResponseDto generateRoadmap(RoadmapPreviewRequestDto request) {
        log.info("Generating roadmap for goal='{}', level='{}'", request.getGoal(), request.getLevel());

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("goal", request.getGoal());
            body.put("level", request.getLevel().getDescription());
            body.put("language", request.getLanguage() != null ? request.getLanguage() : "English");

            String raw = restHttpClientUtil.post(
                    aiServiceBaseApi + GENERATE_ROADMAP_PATH,
                    body,
                    String.class
            );

            // Parse the AI service response and convert to RoadmapPreviewResponseDto
            Map<String, Object> responseData = objectMapper.readValue(raw, Map.class);

            return objectMapper.convertValue(responseData, RoadmapPreviewResponseDto.class);

        } catch (Exception e) {
            log.error("Failed to call AI service for generating roadmap", e);
            throw new RuntimeException("Failed to call AI service for generating roadmap", e);
        }
    }

    @Override
    public TopicContentAiResponseDto generateTopicContent(TopicContentAiRequestDto request) {
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

            String raw = restHttpClientUtil.post(
                    aiServiceBaseApi + GENERATE_TOPIC_CONTENT_PATH,
                    body,
                    String.class
            );

            // Parse the AI service response and convert to TopicContentAiResponseDto
            Map<String, Object> responseData = objectMapper.readValue(raw, Map.class);

            return objectMapper.convertValue(responseData, TopicContentAiResponseDto.class);

        } catch (Exception e) {
            log.error("Failed to call AI service for generating topic content", e);
            throw new RuntimeException("Failed to call AI service for generating topic content", e);
        }
    }

    private RoadmapPreviewResponseDto buildMockRoadmapPreview(RoadmapPreviewRequestDto request) {
        return RoadmapPreviewResponseDto.builder()
                .roadmapTitle("Learn " + request.getGoal())
                .overview("A structured roadmap to master " + request.getGoal() + " from " + request.getLevel() + " level.")
                .estimatedTotalHours(30)
                .chapters(List.of(
                        RoadmapPreviewResponseDto.ChapterPreviewDto.builder()
                                .chapterId("chapter_1")
                                .chapterTitle("Introduction to " + request.getGoal())
                                .objective("Understand the fundamentals.")
                                .topics(List.of(
                                        RoadmapPreviewResponseDto.TopicPreviewDto.builder()
                                                .topicId("topic_1_1")
                                                .topicTitle("Overview and Setup")
                                                .description("Learn the basics and set up your environment.")
                                                .build(),
                                        RoadmapPreviewResponseDto.TopicPreviewDto.builder()
                                                .topicId("topic_1_2")
                                                .topicTitle("Core Concepts")
                                                .description("Understand the core concepts and terminology.")
                                                .build()
                                ))
                                .build(),
                        RoadmapPreviewResponseDto.ChapterPreviewDto.builder()
                                .chapterId("chapter_2")
                                .chapterTitle("Intermediate " + request.getGoal())
                                .objective("Build practical skills.")
                                .topics(List.of(
                                        RoadmapPreviewResponseDto.TopicPreviewDto.builder()
                                                .topicId("topic_2_1")
                                                .topicTitle("Hands-on Practice")
                                                .description("Apply what you learned through exercises.")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private TopicContentAiResponseDto buildMockTopicContent(String topicTitle) {
        return TopicContentAiResponseDto.builder()
                .content("## " + topicTitle + "\n\n"
                        + "This note covers the key concepts of **" + topicTitle + "**.\n\n"
                        + "### Key Points\n"
                        + "- Fundamental concepts and definitions\n"
                        + "- Practical applications\n"
                        + "- Common patterns and best practices\n\n"
                        + "> Content will be generated by AI in Phase 2.")
                .summary(topicTitle + ": covers fundamental concepts, practical applications, and best practices.")
                .build();
    }
}

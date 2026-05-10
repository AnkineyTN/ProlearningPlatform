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

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIRoadmapServiceImpl implements AIRoadmapService {

    @Value("${aiservice.api}")
    private String aiServiceBaseApi;

    private static final String GENERATE_ROADMAP_PATH = "/roadmaps/generate";
    private static final String GENERATE_TOPIC_CONTENT_PATH = "/roadmaps/generate-topic-content";

    private final RestHttpClientUtil restHttpClientUtil;
    private final ObjectMapper objectMapper;

    @Override
    public RoadmapPreviewResponseDto generateRoadmap(RoadmapPreviewRequestDto request) {
        log.info("Generating roadmap for goal='{}', level='{}'", request.getGoal(), request.getLevel());

        // TODO: replace mock with real AI call

        return buildMockRoadmapPreview(request);
    }

    @Override
    public TopicContentAiResponseDto generateTopicContent(TopicContentAiRequestDto request) {
        log.info("Generating topic content for '{}' with {} previous summaries (placeholder)",
                request.getTopicTitle(),
                request.getPreviousSummaries() == null ? 0 : request.getPreviousSummaries().size());

        // TODO: replace mock with real AI call

        return buildMockTopicContent(request.getTopicTitle());
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

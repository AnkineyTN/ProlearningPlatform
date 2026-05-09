package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.request.roadmap.RoadmapPreviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapPreviewResponseDto;
import com.cabybara.prolearningplatform.service.ai.AIRoadmapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIRoadmapServiceImpl implements AIRoadmapService {

    @Value("${aiservice.api}")
    private String aiServiceBaseApi;

    private static final String GENERATE_ROADMAP_PATH = "/roadmaps/generate";

    @Override
    public RoadmapPreviewResponseDto generateRoadmap(RoadmapPreviewRequestDto request) {
        log.info("Generating roadmap preview for goal='{}', level='{}' (mock response)", request.getGoal(), request.getLevel());

        // AI calling

        // Mock response
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
}

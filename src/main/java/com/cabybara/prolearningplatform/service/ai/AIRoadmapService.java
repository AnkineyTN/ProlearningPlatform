package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiRequestDto;
import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiResponseDto;
import com.cabybara.prolearningplatform.dto.internal.roadmap.UserKnowledgeProfileDto;
import com.cabybara.prolearningplatform.dto.request.roadmap.RoadmapPreviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapPreviewResponseDto;

import java.util.List;

public interface AIRoadmapService {
    RoadmapPreviewResponseDto generateRoadmap(RoadmapPreviewRequestDto request, List<UserKnowledgeProfileDto> knowledgeProfiles);
    TopicContentAiResponseDto generateTopicContent(TopicContentAiRequestDto request);
}

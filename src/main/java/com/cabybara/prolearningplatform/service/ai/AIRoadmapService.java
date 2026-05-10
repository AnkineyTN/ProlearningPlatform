package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiRequestDto;
import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiResponseDto;
import com.cabybara.prolearningplatform.dto.request.roadmap.RoadmapPreviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapPreviewResponseDto;

public interface AIRoadmapService {
    RoadmapPreviewResponseDto generateRoadmap(RoadmapPreviewRequestDto request);
    TopicContentAiResponseDto generateTopicContent(TopicContentAiRequestDto request);
}

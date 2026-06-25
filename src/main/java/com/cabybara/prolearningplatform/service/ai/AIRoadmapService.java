package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiRequestDto;
import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiResponseDto;
import com.cabybara.prolearningplatform.dto.internal.roadmap.UserKnowledgeProfileDto;
import com.cabybara.prolearningplatform.dto.request.roadmap.RoadmapPreviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapPreviewResponseDto;

import java.util.List;

public interface AIRoadmapService {
    /** Sync call (roadmap preview): resolves the current authenticated user's active LLM config. */
    RoadmapPreviewResponseDto generateRoadmap(RoadmapPreviewRequestDto request, List<UserKnowledgeProfileDto> knowledgeProfiles);

    /** Async/no-context call: caller supplies the resolved LLM config (by owning user id). */
    TopicContentAiResponseDto generateTopicContent(TopicContentAiRequestDto request, DecryptedLlmConfig cfg);
}

package com.cabybara.prolearningplatform.service.roadmap;

import com.cabybara.prolearningplatform.dto.request.roadmap.AcceptRoadmapRequestDto;
import com.cabybara.prolearningplatform.dto.request.roadmap.RoadmapPreviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapDetailResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapListItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapPreviewResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.TopicCompleteResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.TopicStartResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoadmapService {

    RoadmapPreviewResponseDto previewRoadmap(Long userId, RoadmapPreviewRequestDto request);

    RoadmapDetailResponseDto acceptRoadmap(Long userId, AcceptRoadmapRequestDto dto);

    Page<RoadmapListItemResponseDto> getRoadmaps(Long userId, Pageable pageable);

    RoadmapDetailResponseDto getRoadmap(Long userId, Long roadmapId);

    TopicStartResponseDto startTopic(Long userId, Long roadmapId, Long topicId);

    TopicCompleteResponseDto markTopicComplete(Long userId, Long roadmapId, Long topicId);

    void abandonRoadmap(Long userId, Long roadmapId);
}

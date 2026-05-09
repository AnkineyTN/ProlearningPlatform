package com.cabybara.prolearningplatform.dto.response.roadmap;

import com.cabybara.prolearningplatform.enums.RoadmapStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class RoadmapListItemResponseDto {

    private Long id;
    private String title;
    private String overview;
    private RoadmapStatus status;
    private Integer estimatedTotalHours;
    private int totalChapters;
    private int completedChapters;
    private long totalTopics;
    private long completedTopics;
    private int progressPercent;
    private OffsetDateTime createdAt;
}

package com.cabybara.prolearningplatform.dto.response.roadmap;

import com.cabybara.prolearningplatform.enums.ChapterStatus;
import com.cabybara.prolearningplatform.enums.RoadmapStatus;
import com.cabybara.prolearningplatform.enums.TopicContentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapDetailResponseDto {

    private Long id;
    private String title;
    private String overview;
    private RoadmapStatus status;
    private Integer estimatedTotalHours;
    private long totalTopics;
    private long completedTopics;
    private int progressPercent;
    private OffsetDateTime createdAt;
    private List<ChapterDetailDto> chapters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterDetailDto {
        private Long id;
        private String chapterKey;
        private String title;
        private String objective;
        private Integer orderIndex;
        private ChapterStatus status;
        private long totalTopics;
        private long completedTopics;
        private int progressPercent;
        private List<TopicDetailDto> topics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicDetailDto {
        private Long id;
        private String topicKey;
        private String title;
        private String description;
        private Integer orderIndex;
        private Boolean completed;
        private TopicContentStatus contentStatus;
        private Long setId;
    }
}

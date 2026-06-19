package com.cabybara.prolearningplatform.dto.response.roadmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapPreviewResponseDto {

    @JsonProperty("roadmap_title")
    private String roadmapTitle;

    private String overview;

    @JsonProperty("estimated_total_hours")
    private Integer estimatedTotalHours;

    private List<ChapterPreviewDto> chapters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterPreviewDto {

        @JsonProperty("chapter_id")
        private String chapterId;

        @JsonProperty("chapter_title")
        private String chapterTitle;

        private String objective;

        private List<TopicPreviewDto> topics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicPreviewDto {

        @JsonProperty("topic_id")
        private String topicId;

        @JsonProperty("topic_title")
        private String topicTitle;

        private String description;
    }
}

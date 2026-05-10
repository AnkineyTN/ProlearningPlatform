package com.cabybara.prolearningplatform.dto.request.roadmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AcceptRoadmapRequestDto {

    @NotBlank
    @JsonProperty("roadmap_title")
    private String roadmapTitle;

    private String overview;

    @JsonProperty("estimated_total_hours")
    private Integer estimatedTotalHours;

    @NotEmpty
    private List<ChapterDto> chapters;

    @Data
    public static class ChapterDto {

        @JsonProperty("chapter_id")
        private String chapterId;

        @NotBlank
        @JsonProperty("chapter_title")
        private String chapterTitle;

        private String objective;

        @NotEmpty
        private List<TopicDto> topics;
    }

    @Data
    public static class TopicDto {

        @JsonProperty("topic_id")
        private String topicId;

        @NotBlank
        @JsonProperty("topic_title")
        private String topicTitle;

        private String description;
    }
}

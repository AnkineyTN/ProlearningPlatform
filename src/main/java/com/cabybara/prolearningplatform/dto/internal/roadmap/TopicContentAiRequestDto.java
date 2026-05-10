package com.cabybara.prolearningplatform.dto.internal.roadmap;

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
public class TopicContentAiRequestDto {

    @JsonProperty("topic_title")
    private String topicTitle;

    @JsonProperty("description")
    private String description;

    @JsonProperty("chapter_title")
    private String chapterTitle;

    @JsonProperty("chapter_objective")
    private String chapterObjective;

    @JsonProperty("roadmap_title")
    private String roadmapTitle;

    @JsonProperty("previous_summaries")
    private List<SummaryContextDto> previousSummaries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryContextDto {
        private String title;
        private String summary;
    }
}

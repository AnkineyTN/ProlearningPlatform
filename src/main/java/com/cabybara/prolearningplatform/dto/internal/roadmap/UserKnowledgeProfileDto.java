package com.cabybara.prolearningplatform.dto.internal.roadmap;

import com.cabybara.prolearningplatform.dto.response.knowledge.TopicAccuracyDto;
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
public class UserKnowledgeProfileDto {

    @JsonProperty("set_id")
    private Long setId;

    @JsonProperty("topic_accuracies")
    private List<TopicAccuracyDto> topicAccuracies;

    @JsonProperty("strengths")
    private String strengths;

    @JsonProperty("weaknesses")
    private String weaknesses;

    @JsonProperty("improvements")
    private String improvements;
}

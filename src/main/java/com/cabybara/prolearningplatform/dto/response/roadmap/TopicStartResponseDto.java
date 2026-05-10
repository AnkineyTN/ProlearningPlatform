package com.cabybara.prolearningplatform.dto.response.roadmap;

import com.cabybara.prolearningplatform.enums.TopicContentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicStartResponseDto {

    private Long topicId;
    private Long setId;
    private TopicContentStatus contentStatus;
}

package com.cabybara.prolearningplatform.dto.internal.roadmap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicContentAiResponseDto {
    private String content;
    private String summary;
}

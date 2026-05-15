package com.cabybara.prolearningplatform.dto.response.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateExamByAIResponseDto {
    private String title;
    private String description;
    private Integer duration;
    private String content;
}


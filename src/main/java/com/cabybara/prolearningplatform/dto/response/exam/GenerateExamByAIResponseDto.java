package com.cabybara.prolearningplatform.dto.response.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GenerateExamByAIResponseDto {
    private String content;
}


package com.cabybara.prolearningplatform.dto.response.exam;

import lombok.Builder;

import java.util.List;

@Builder
public record QuestionListResponseDto(
        List<QuestionResponseDto> questions
) {
}

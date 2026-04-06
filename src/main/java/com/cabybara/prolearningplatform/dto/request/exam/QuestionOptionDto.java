package com.cabybara.prolearningplatform.dto.request.exam;

import lombok.Builder;

@Builder
public record QuestionOptionDto(
    Long id,
    String optionText,
    Boolean isCorrect
) {}
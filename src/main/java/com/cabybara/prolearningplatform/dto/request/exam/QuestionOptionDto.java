package com.cabybara.prolearningplatform.dto.request.exam;

import lombok.Builder;

@Builder
public record QuestionOptionDto(
    String optionText,
    Boolean isCorrect
) {}
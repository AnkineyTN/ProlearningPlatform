package com.cabybara.prolearningplatform.dto.request.exam;

import jakarta.validation.constraints.NotNull;

public record AnswerSubmissionDto(
        @NotNull Long questionId,
        Long selectedOptionId,
        String essayAnswer
) {}

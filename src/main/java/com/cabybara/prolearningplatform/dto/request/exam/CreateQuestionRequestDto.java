package com.cabybara.prolearningplatform.dto.request.exam;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateQuestionRequestDto(
        @NotNull(message = "Content cannot be null")
        String content,

        @NotNull(message = "Question type cannot be null")
        String type,

        @Min(value = 1, message = "Points must be at least 1")
        Integer point,

        List<QuestionOptionDto> options
) {}

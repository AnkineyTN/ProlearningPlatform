package com.cabybara.prolearningplatform.dto.request.exam;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateExamFromReviewRequestDto(
        @NotNull(message = "Title cannot be null")
        String title,

        String description,

        @NotNull(message = "Duration cannot be null")
        Long duration,

        List<CreateQuestionRequestDto> questions
) {}

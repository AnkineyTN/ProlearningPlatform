package com.cabybara.prolearningplatform.dto.request.exam;

import com.cabybara.prolearningplatform.enums.Privacy;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateExamRequestDto(
        @NotNull(message = "Title cannot be null")
        String title,
        Privacy privacy,
        String description,
        @NotNull(message = "Duration cannot be null")
        Long duration
) {}

package com.cabybara.prolearningplatform.dto.request.exam;

import com.cabybara.prolearningplatform.enums.Privacy;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateQuizRequestDto(
        @NotNull(message = "Title cannot be null")
        String title,
        Privacy privacy,
        String description
) {}

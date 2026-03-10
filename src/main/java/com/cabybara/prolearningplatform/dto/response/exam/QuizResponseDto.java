package com.cabybara.prolearningplatform.dto.response.exam;

import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record QuizResponseDto(
        Long id,
        String title,
        Privacy privacy,
        String description,
        Long duration,
        Long createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {}

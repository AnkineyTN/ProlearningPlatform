package com.cabybara.prolearningplatform.dto.response.exam;

import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.Builder;

@Builder
public record QuizResponseDto(
        Long id,
        String title,
        Privacy privacy,
        String description,
        Long createdBy
) {}

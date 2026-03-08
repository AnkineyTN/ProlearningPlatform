package com.cabybara.prolearningplatform.dto.request.exam;

import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.Builder;

@Builder
public record CreateQuizRequestDto(
        String title,
        Privacy privacy,
        String description
) {}

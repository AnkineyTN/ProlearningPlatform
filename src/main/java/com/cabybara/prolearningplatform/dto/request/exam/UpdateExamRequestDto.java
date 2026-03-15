package com.cabybara.prolearningplatform.dto.request.exam;

import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.Builder;

@Builder
public record UpdateExamRequestDto(
        String title,
        Privacy privacy,
        String description,
        Long duration
) {}
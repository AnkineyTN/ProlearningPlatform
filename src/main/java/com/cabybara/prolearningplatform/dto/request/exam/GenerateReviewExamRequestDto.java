package com.cabybara.prolearningplatform.dto.request.exam;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record GenerateReviewExamRequestDto(
        @NotEmpty List<Long> questionIds
) {}

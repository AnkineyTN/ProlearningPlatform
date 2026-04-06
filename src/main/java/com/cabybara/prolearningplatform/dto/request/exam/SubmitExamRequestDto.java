package com.cabybara.prolearningplatform.dto.request.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitExamRequestDto(
        @NotNull @NotEmpty @Valid List<AnswerSubmissionDto> answers
) {}

package com.cabybara.prolearningplatform.dto.response.exam;

import com.cabybara.prolearningplatform.enums.ExamAttemptStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ExamAttemptResultDto(
        Long id,
        Long examId,
        ExamAttemptStatus status,
        LocalDateTime startedAt,
        LocalDateTime deadlineAt,
        LocalDateTime submittedAt,
        Double score,
        Integer totalPoints,
        List<ExamAnswerResultDto> answers
) {}

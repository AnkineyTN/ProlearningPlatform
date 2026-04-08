package com.cabybara.prolearningplatform.dto.response.exam;

import com.cabybara.prolearningplatform.enums.ExamAttemptStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExamAttemptDto(
        Long id,
        Long examId,
        ExamAttemptStatus status,
        LocalDateTime startedAt,
        LocalDateTime deadlineAt,
        LocalDateTime submittedAt,
        Double score,
        Integer totalPoints
) {}

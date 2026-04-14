package com.cabybara.prolearningplatform.dto.response.exam;

public record QuestionErrorStatDto(
        Long questionId,
        String questionText,
        int totalAttempts,
        int correctCount,
        int incorrectCount,
        double incorrectRate
) {}

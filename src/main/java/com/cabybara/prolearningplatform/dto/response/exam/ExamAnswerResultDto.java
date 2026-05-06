package com.cabybara.prolearningplatform.dto.response.exam;

public record ExamAnswerResultDto(
        Long questionId,
        String questionContent,
        Long selectedOptionId,
        String studentAnswer,
        Boolean isCorrect,
        String expectedAnswer,
        Double earnedPoints,
        String feedback
) {}

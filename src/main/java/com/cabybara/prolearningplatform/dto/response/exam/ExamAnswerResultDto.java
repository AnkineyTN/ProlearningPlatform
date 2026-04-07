package com.cabybara.prolearningplatform.dto.response.exam;

public record ExamAnswerResultDto(
        Long questionId,
        Long selectedOptionId,
        String essayAnswer,
        Boolean isCorrect,
        String expectedAnswer
) {}

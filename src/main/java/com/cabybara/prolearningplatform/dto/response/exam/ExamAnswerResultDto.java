package com.cabybara.prolearningplatform.dto.response.exam;

public record ExamAnswerResultDto(
        Long questionId,
        Long selectedOptionId,
        String essayAnswer,
        // NULL means pending AI grading (ESSAY). TRUE/FALSE for auto-graded types.
        Boolean isCorrect
) {}

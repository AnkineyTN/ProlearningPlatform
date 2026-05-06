package com.cabybara.prolearningplatform.dto.request.exam;

import com.cabybara.prolearningplatform.enums.QuestionType;
import lombok.Builder;

import java.util.List;

@Builder
public record UpdateQuestionRequestDto(
        String content,
        QuestionType type,
        List<QuestionOptionDto> options,
        // Only applicable for ESSAY type questions
        String expectedAnswer,
        String topic
) {}


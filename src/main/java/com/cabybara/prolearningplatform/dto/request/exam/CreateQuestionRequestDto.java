package com.cabybara.prolearningplatform.dto.request.exam;

import com.cabybara.prolearningplatform.enums.QuestionType;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateQuestionRequestDto(
        String content,
        QuestionType type,
        Integer point,
        List<QuestionOptionDto> options
) {}

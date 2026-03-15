package com.cabybara.prolearningplatform.dto.response.exam;

import com.cabybara.prolearningplatform.dto.request.exam.QuestionOptionDto;
import com.cabybara.prolearningplatform.enums.QuestionType;
import lombok.Builder;

import java.util.List;

@Builder
public record QuestionResponseDto (
    Long id,
    String content,
    QuestionType type,
    Integer points,
    List<QuestionOptionDto> options
) {}

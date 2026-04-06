package com.cabybara.prolearningplatform.dto.response.exam;

import com.cabybara.prolearningplatform.enums.QuestionType;

import java.util.List;

// Question DTO for exam-taking view
public record ExamQuestionViewDto(
        Long id,
        String content,
        QuestionType type,
        Integer points,
        List<ExamOptionViewDto> options
) {}

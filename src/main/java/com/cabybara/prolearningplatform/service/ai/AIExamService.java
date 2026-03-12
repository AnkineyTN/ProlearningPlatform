package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;

import java.util.Map;

public interface AIExamService {
    public GenerateExamByAIResponseDto generateExam(Map<String, Object> requestBody);
}

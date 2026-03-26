package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByWebRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.enums.Language;

import java.util.List;
import java.util.Map;

public interface AIExamService {
    public GenerateExamByAIResponseDto generateExamByFiles(GenerateExamByFileRequestDto request);

    public GenerateExamByAIResponseDto generateExamByNotes(List<String> contents, Map<String, Integer> questions, String freeText, Language language);

    public GenerateExamByAIResponseDto generateExamByWeb(GenerateExamByWebRequestDto request);
}

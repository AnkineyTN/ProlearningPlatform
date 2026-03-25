package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByWebRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.enums.Language;

import java.util.List;

public interface AIFlashcardService {
    public GenerateFlashcardByAIResponseDto generateFlashcardByFiles(GenerateFlashcardByFileRequestDto request);

    public GenerateFlashcardByAIResponseDto generateFlashcardByNotes(List<String> contents, String freeText, Language language);

    public GenerateFlashcardByAIResponseDto generateFlashcardByWeb(GenerateFlashcardByWebRequestDto request);
}

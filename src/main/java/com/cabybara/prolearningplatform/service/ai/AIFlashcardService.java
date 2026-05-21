package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.CardContent;
import com.cabybara.prolearningplatform.dto.request.flashcard.AIGenerateFlashcardByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByWebRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.enums.Language;

import java.util.List;

public interface AIFlashcardService {
    GenerateFlashcardByAIResponseDto generateFlashcardByFiles(GenerateFlashcardByFileRequestDto request);

    GenerateFlashcardByAIResponseDto generateFlashcardByNotes(AIGenerateFlashcardByNoteRequestDto request);

    GenerateFlashcardByAIResponseDto generateFlashcardByWeb(GenerateFlashcardByWebRequestDto request);
}

package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.note.ConvertFileToVectorRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.ExplainNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.SummarizeFileRequestDTO;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.note.ExplainNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.SummarizeFileResponseDTO;

public interface AIFlashcardService {
    public GenerateFlashcardByAIResponseDto generateFlashcard(String content, String type);
}

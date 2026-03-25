package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface FlashcardService {
    Page<FlashcardResponseDto> getAllFlashcard(Long setId, String q, Privacy privacy, Pageable pageable);

    DetailFlashcardResponseDto getDetailFlashcard(Long setId, Long flashcardId);

    FlashcardResponseDto addFlashcardManual(Long setId, FlashcardCreateRequestDto flashcardCreateRequestDto);

    void deleteFlashcard(Long setId, Long flashcardId) throws BadRequestException;

    Flashcard getFlashcardById(Long flashcardId);

    FlashcardResponseDto updateFlashcard(Long setId, Long flashcardId, FlashcardUpdatingRequestDto flashcardUpdatingRequestDto) throws BadRequestException;

    DetailFlashcardResponseDto updateFlashcard(Flashcard newFlashcard);

    GenerateFlashcardByAIResponseDto generateFlashcardByFiles(GenerateFlashcardByFileRequestDto request);

    GenerateFlashcardByAIResponseDto generateFlashcardByNotes(GenerateFlashcardByNoteRequestDto request);
}

package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.FlashcardCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.FlashcardUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardResponseDto;
import com.cabybara.prolearningplatform.model.Flashcard;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface FlashcardService {
    Page<FlashcardResponseDto> getAllFlashcard(Long setId, Pageable pageable);

    DetailFlashcardResponseDto getDetailFlashcard(Long setId, Long flashcardId);

    FlashcardResponseDto addFlashcardManual(Long setId, FlashcardCreateRequestDto flashcardCreateRequestDto);

    void deleteFlashcard(Long setId, Long flashcardId) throws BadRequestException;

    Flashcard getFlashcardById(Long flashcardId);

    FlashcardResponseDto updateFlashcard(Long setId, Long flashcardId, FlashcardUpdatingRequestDto flashcardUpdatingRequestDto) throws BadRequestException;

    DetailFlashcardResponseDto updateFlashcard(Flashcard newFlashcard);
}

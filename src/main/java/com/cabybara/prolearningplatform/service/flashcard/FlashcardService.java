package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.FlashcardCreateRequestDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardResponseDto;
import com.cabybara.prolearningplatform.model.Flashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface FlashcardService {
    Page<FlashcardResponseDto> getAllFlashcard(Long setId, Pageable pageable);

    DetailFlashcardResponseDto getDetailFlashcard(Long setId, Long flashcardId);

    FlashcardResponseDto addFlashcardManual(Long setId, FlashcardCreateRequestDto flashcardCreateRequestDto);

    DetailFlashcardResponseDto addCardToFlashcard(Long setId, Long flashcardId, List<CardItemCreateRequestDto> dtos);
}

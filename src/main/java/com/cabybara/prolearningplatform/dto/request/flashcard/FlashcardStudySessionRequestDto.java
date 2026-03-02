package com.cabybara.prolearningplatform.dto.request.flashcard;

import com.cabybara.prolearningplatform.dto.response.flashcard.CardLearnResponseDto;
import lombok.Data;

import java.util.List;

@Data
public class FlashcardStudySessionRequestDto {
    private Long sessionId;
    private List<CardLearnResponseDto> cards;
}

package com.cabybara.prolearningplatform.dto.response.flashcard;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class FlashcardStudySessionLogItemResponseDto {
    private Long cardId;
    private boolean known;
    private OffsetDateTime reviewedAt;
}
package com.cabybara.prolearningplatform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
public class FlashcardStudySessionLogItemResponseDto {
    private Long cardId;
    private boolean known;
    private OffsetDateTime reviewedAt;
}
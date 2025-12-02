package com.cabybara.prolearningplatform.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlashcardStudySessionLogItemResponseDto {
    private Long cardId;
    private boolean isKnown;
    private LocalDateTime reviewedAt;
}
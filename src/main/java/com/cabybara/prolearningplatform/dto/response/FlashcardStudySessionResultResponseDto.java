package com.cabybara.prolearningplatform.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class FlashcardStudySessionResultResponseDto {
    private Long sessionId;
    private Integer correctCount;
    private Integer incorrectCount;
    private Instant finishedAt;
    private List<FlashcardStudySessionLogItemResponseDto> logs;
}

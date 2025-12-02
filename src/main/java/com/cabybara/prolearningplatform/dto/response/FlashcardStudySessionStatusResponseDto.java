package com.cabybara.prolearningplatform.dto.response;

import com.cabybara.prolearningplatform.enums.FlashcardStudySessionStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlashcardStudySessionStatusResponseDto {
    private Long id;
    private FlashcardStudySessionStatus status;
    private Long totalCards;
    private Long completedCount;
    private Long remainingCount;
    private Long progressPercent;
}

package com.cabybara.prolearningplatform.dto.response.flashcard;

import com.cabybara.prolearningplatform.enums.FlashcardStudySessionStatus;
import com.cabybara.prolearningplatform.enums.StudyMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlashcardStudySessionStatusResponseDto {
    private Long id;
    private FlashcardStudySessionStatus status;
    private StudyMode studyMode;
    private Long totalCards;
    private Long completedCount;
    private Long remainingCount;
    private Long progressPercent;
}

package com.cabybara.prolearningplatform.model.flashcard_study_session;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
public class FlashcardStudySessionLogItem implements Serializable {
    private Long cardId;
    private boolean isKnown;
    private OffsetDateTime reviewedAt;
}
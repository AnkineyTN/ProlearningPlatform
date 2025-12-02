package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.FlashcardStudySessionSyncRequestDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionResultResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionStartResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionStatusResponseDto;
import org.apache.coyote.BadRequestException;

public interface FlashcardStudySessionService {
    FlashcardStudySessionStatusResponseDto checkStudySessionStatus(Long flashcardId);

    FlashcardStudySessionStartResponseDto startOrResumeSession(Long setId, Long flashcardId) throws BadRequestException;

    FlashcardStudySessionResultResponseDto getSessionResult(Long sessionId) throws BadRequestException;

    void syncSessionProgress(Long sessionId, FlashcardStudySessionSyncRequestDto syncRequestDto) throws BadRequestException;
}

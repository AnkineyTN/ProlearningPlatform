package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardStudySessionSyncRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionResultResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionStartResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionStatusResponseDto;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface FlashcardStudySessionService {
    List<FlashcardStudySessionStatusResponseDto> checkStudySessionStatus(Long setId, Long flashcardId);

    FlashcardStudySessionStartResponseDto startOrResumeSession(Long setId, Long flashcardId) throws BadRequestException;

    FlashcardStudySessionResultResponseDto getSessionResult(Long sessionId) throws BadRequestException;

    FlashcardStudySessionStatusResponseDto syncSessionProgress(Long sessionId, FlashcardStudySessionSyncRequestDto syncRequestDto) throws BadRequestException;

    void cancelSession(Long sessionId) throws BadRequestException;
}

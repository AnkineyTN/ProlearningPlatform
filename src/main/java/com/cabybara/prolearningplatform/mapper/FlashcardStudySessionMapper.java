package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionLogItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionResultResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionStartResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardStudySessionStatusResponseDto;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.model.flashcard_study_session.FlashcardStudySession;
import com.cabybara.prolearningplatform.model.flashcard_study_session.FlashcardStudySessionLogItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = CardItemMapper.class
)
public interface FlashcardStudySessionMapper {
    @Mapping(target = "totalCards", expression = "java(mapListToCount(flashcardStudySession.getInitialCardIds()))")
    @Mapping(target = "completedCount", expression = "java(mapListToCount(flashcardStudySession.getReviewLog()))")
    @Mapping(target = "remainingCount", expression = "java(mapListToCount(flashcardStudySession.getRemainingCardIds()))")
    @Mapping(target = "progressPercent", expression = "java(calculateProgressPercent(flashcardStudySession))")
    FlashcardStudySessionStatusResponseDto toFlashcardStudySessionStatusResponse(FlashcardStudySession flashcardStudySession);

    @Mapping(source = "flashcardStudySession.id", target = "id")
    @Mapping(source = "flashcardStudySession.studyMode", target = "studyMode")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "cards", target = "cards")
    FlashcardStudySessionStartResponseDto toFlashcardStudySessionStartResponse(FlashcardStudySession flashcardStudySession, List<CardItem> cards, String message);

    @Mapping(source = "id", target = "sessionId")
    @Mapping(source = "lastInteractionAt", target = "finishedAt")
    @Mapping(source = "reviewLog", target = "logs")
    FlashcardStudySessionResultResponseDto toFlashcardStudySessionResultResponse(FlashcardStudySession flashcardStudySession);

    FlashcardStudySessionLogItemResponseDto toFlashcardStudySessionLogItemResponse(FlashcardStudySessionLogItem flashcardStudySessionLogItem);

    default Long mapListToCount(List<?> list) {
        return list != null ? (long) list.size() : 0L;
    }

    default Long calculateProgressPercent(FlashcardStudySession session) {
        Long totalCards = mapListToCount(session.getInitialCardIds());
        Long completedCount = mapListToCount(session.getReviewLog());

        if (totalCards == null || totalCards == 0L) {
            return 0L;
        }

        return Math.round((completedCount * 100.0) / totalCards);
    }

    default List<FlashcardStudySessionLogItemResponseDto> sessionLogToDto(List<FlashcardStudySessionLogItem> sessionLogItems) {
        return sessionLogItems.stream().map(this::toFlashcardStudySessionLogItemResponse).toList();
    }
}

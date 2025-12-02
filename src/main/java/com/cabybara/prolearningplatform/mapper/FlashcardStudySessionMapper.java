package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionResultResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionStartResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardStudySessionStatusResponseDto;
import com.cabybara.prolearningplatform.model.CardItem;
import com.cabybara.prolearningplatform.model.flashcard_study_session.FlashcardStudySession;
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

    FlashcardStudySessionStartResponseDto toFlashcardStudySessionStartResponse(FlashcardStudySession flashcardStudySession, List<CardItem> cards);

    FlashcardStudySessionResultResponseDto toFlashcardStudySessionResultResponse(FlashcardStudySession flashcardStudySession);

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
}

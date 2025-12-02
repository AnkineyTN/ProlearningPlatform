package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.FlashcardStudySessionStatus;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.flashcard_study_session.FlashcardStudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlashcardStudySessionRepository extends JpaRepository<FlashcardStudySession, Long> {
    Optional<FlashcardStudySession> findByUserIdAndFlashcardIdAndStatus(
            Long userId,
            Long flashcardSetId,
            FlashcardStudySessionStatus status
    );

    Long user(User user);
}

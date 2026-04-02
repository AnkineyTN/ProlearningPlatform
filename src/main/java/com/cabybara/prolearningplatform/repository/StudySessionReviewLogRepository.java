package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.flashcard_study_session.StudySessionReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudySessionReviewLogRepository
        extends JpaRepository<StudySessionReviewLog, Long> {

    long countBySessionId(Long sessionId);

    List<StudySessionReviewLog> findBySessionIdOrderByReviewedAtAsc(Long sessionId);
}
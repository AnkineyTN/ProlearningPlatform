package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.exam.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    List<ExamAttempt> findAllByExamIdAndUserIdOrderByStartedAtDesc(Long examId, Long userId);

    Optional<ExamAttempt> findByIdAndUserId(Long id, Long userId);
}

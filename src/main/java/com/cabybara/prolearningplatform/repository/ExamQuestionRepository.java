package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.exam.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    Optional<ExamQuestion> findByExamIdAndQuestionId(Long examId, Long questionId);

    void deleteByExamIdAndQuestionId(Long examId, Long questionId);

    int countByExamId(Long examId);
}


package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.exam.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    @Query("""
    SELECT eq FROM ExamQuestion eq
    JOIN FETCH eq.question q
    LEFT JOIN FETCH q.options
    WHERE eq.exam.id = :examId
    ORDER BY eq.orderIndex
    """)
    List<ExamQuestion> findAllByExamId(Long examId);

    Optional<ExamQuestion> findByExamIdAndQuestionId(Long examId, Long questionId);

    void deleteByExamIdAndQuestionId(Long examId, Long questionId);

    int countByExamId(Long examId);
}


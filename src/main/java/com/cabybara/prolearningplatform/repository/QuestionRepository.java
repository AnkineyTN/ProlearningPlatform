package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.exam.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q " +
            "JOIN ExamQuestion qq ON qq.question = q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE qq.exam.id = :examId " +
            "ORDER BY qq.orderIndex ASC")
    List<Question> findAllByExamId(Long examId);

    List<Question> findAllByCreatedBy(Long createdBy);
}

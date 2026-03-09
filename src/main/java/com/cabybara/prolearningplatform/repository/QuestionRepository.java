package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.exam.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q JOIN QuizQuestion qq ON qq.question = q WHERE qq.quiz.id = :quizId ORDER BY qq.orderIndex ASC")
    List<Question> findAllByQuizId(Long quizId);

    List<Question> findAllByCreatedBy(Long createdBy);
}

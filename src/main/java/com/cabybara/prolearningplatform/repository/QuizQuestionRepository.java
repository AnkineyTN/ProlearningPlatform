package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.exam.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    Optional<QuizQuestion> findByQuizIdAndQuestionId(Long quizId, Long questionId);

    void deleteByQuizIdAndQuestionId(Long quizId, Long questionId);

    int countByQuizId(Long quizId);
}


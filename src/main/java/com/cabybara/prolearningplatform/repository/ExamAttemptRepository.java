package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.QuestionErrorStat;
import com.cabybara.prolearningplatform.model.exam.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    List<ExamAttempt> findAllByExamIdAndUserIdOrderByStartedAtDesc(Long examId, Long userId);

    Optional<ExamAttempt> findByIdAndUserId(Long id, Long userId);

    @Query(nativeQuery = true, value = """
            SELECT
                a.question_id                                                   AS questionId,
                q.content                                                       AS questionText,
                COUNT(a.id)                                                     AS totalAttempts,
                SUM(CASE
                    WHEN a.is_correct = FALSE THEN 1
                    WHEN a.is_correct IS NULL
                         AND eq.points > 0
                         AND a.earned_points / eq.points < :essayThreshold THEN 1
                    ELSE 0
                END)                                                            AS incorrectCount
            FROM exam_answers a
            JOIN exam_attempts att ON a.attempt_id = att.id
            JOIN questions q ON a.question_id = q.id
            JOIN exam_questions eq ON eq.question_id = a.question_id AND eq.exam_id = :examId
            WHERE att.exam_id  = :examId
              AND att.user_id  = :userId
              AND att.status   = 'SUBMITTED'
              AND (
                  a.is_correct IS NOT NULL
                  OR (a.essay_answer IS NOT NULL AND a.earned_points IS NOT NULL)
              )
            GROUP BY a.question_id, q.content
            ORDER BY incorrectCount DESC
            """)
    List<QuestionErrorStat> findQuestionStats(
            @Param("examId") Long examId,
            @Param("userId") Long userId,
            @Param("essayThreshold") double essayThreshold);
}

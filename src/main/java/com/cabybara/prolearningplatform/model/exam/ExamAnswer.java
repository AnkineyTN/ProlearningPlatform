package com.cabybara.prolearningplatform.model.exam;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "exam_answers")
@Getter
@Setter
public class ExamAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private ExamAttempt attempt;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    //null for essay question
    @Column(name = "selected_option_id")
    private Long selectedOptionId;

    // null for non-essay question
    @Column(name = "essay_answer", columnDefinition = "TEXT")
    private String essayAnswer;

    // NULL = pending AI grading (ESSAY type). TRUE/FALSE others.
    @Column(name = "is_correct")
    private Boolean isCorrect;
}

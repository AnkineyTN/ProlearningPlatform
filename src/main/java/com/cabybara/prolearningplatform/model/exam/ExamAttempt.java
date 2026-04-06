package com.cabybara.prolearningplatform.model.exam;

import com.cabybara.prolearningplatform.enums.ExamAttemptStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_attempts")
@Getter
@Setter
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private ExamAttemptStatus status = ExamAttemptStatus.IN_PROGRESS;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    // NULL when exam has no duration limit
    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamAnswer> answers = new ArrayList<>();
}

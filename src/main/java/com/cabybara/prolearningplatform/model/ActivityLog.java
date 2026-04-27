package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.ContentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "activity_log", indexes = {
        @Index(name = "idx_activity_log_user_date", columnList = "user_id, date"),
        @Index(name = "idx_activity_log_user_content_type", columnList = "user_id, content_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "content_type", nullable = false, columnDefinition = "activity_content_type")
    private ContentType contentType;

    @Column(name = "set_id")
    private Long setId;

    @Column(name = "todo_id")
    private Long todoId;

    @Column(name = "active_duration", nullable = false)
    private Long activeDuration;

    @Column(name = "raw_duration", nullable = false)
    private Long rawDuration;

    @Column(name = "score")
    private Integer score;

    @Column(name = "items_count", nullable = false)
    private Integer itemsCount;
}

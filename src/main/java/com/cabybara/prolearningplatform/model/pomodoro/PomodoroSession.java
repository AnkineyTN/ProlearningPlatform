package com.cabybara.prolearningplatform.model.pomodoro;

import java.time.OffsetDateTime;

import com.cabybara.prolearningplatform.enums.PomodoroSessionType;
import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
@Entity
@Table(name = "pomodoro_session")
public class PomodoroSession extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PomodoroSessionType type;

    @Column(name = "duration", nullable = false)
    private Integer duration;   // giây thực tế

    @Column(name = "planned", nullable = false)
    private Integer planned;    // giây dự kiến

    @Column(name = "completed", nullable = false)
    @Builder.Default
    private Boolean completed = true;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private OffsetDateTime endedAt;
}
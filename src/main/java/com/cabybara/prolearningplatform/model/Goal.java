package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.GoalStatus;
import com.cabybara.prolearningplatform.enums.GoalType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "goal")
public class Goal extends AbstractEntity {

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(length = 10)
    private String color;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GoalStatus status = GoalStatus.IN_PROGRESS;

    @Column(name = "type", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GoalType type = GoalType.LONG;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_goal_id")
    private Goal parentGoal;

    @OneToMany(mappedBy = "parentGoal", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Goal> shortGoals = new ArrayList<>();

    @OneToMany(mappedBy = "goal", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Todo> todos = new ArrayList<>();
}

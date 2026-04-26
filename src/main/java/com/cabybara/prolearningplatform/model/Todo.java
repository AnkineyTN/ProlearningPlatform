package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.converter.ResourceRefListConverter;
import com.cabybara.prolearningplatform.enums.TodoPriority;
import com.cabybara.prolearningplatform.enums.TodoStatus;
import com.cabybara.prolearningplatform.enums.TodoType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "todo")
public class Todo extends AbstractEntity {

    @Column(length = 500, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TodoPriority priority = TodoPriority.MEDIUM;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "type", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TodoType type = TodoType.DAILY;

    @Column(name = "status", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TodoStatus status = TodoStatus.TODO;

    @Convert(converter = ResourceRefListConverter.class)
    @Column(name = "set_refs", columnDefinition = "TEXT")
    @Builder.Default
    private List<ResourceRef> setRefs = new ArrayList<>();

    @Convert(converter = ResourceRefListConverter.class)
    @Column(name = "note_refs", columnDefinition = "TEXT")
    @Builder.Default
    private List<ResourceRef> noteRefs = new ArrayList<>();

    @Convert(converter = ResourceRefListConverter.class)
    @Column(name = "flashcard_refs", columnDefinition = "TEXT")
    @Builder.Default
    private List<ResourceRef> flashcardRefs = new ArrayList<>();

    @Convert(converter = ResourceRefListConverter.class)
    @Column(name = "exam_refs", columnDefinition = "TEXT")
    @Builder.Default
    private List<ResourceRef> examRefs = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;
}

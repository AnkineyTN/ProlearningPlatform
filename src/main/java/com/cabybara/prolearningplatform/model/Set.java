package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.exam.Quiz;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import com.cabybara.prolearningplatform.model.note.Note;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "set")
public class Set extends AbstractEntity {
    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Privacy privacy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "set", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Note> notes;

    @OneToMany(mappedBy = "set", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Flashcard> flashcards;

    @OneToMany(mappedBy = "set", cascade = CascadeType.ALL)
    private List<Quiz> quizzes = new ArrayList<>();
}

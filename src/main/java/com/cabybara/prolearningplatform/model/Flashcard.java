package com.cabybara.prolearningplatform.model;

import com.cabybara.prolearningplatform.enums.FlashcardStatus;
import com.cabybara.prolearningplatform.enums.Privacy;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flashcard")
public class Flashcard extends AbstractEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FlashcardStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy", nullable = false)
    private Privacy privacy;

    @Column(name = "last_study")
    private LocalDateTime lastStudy;

    @Column(name = "known", nullable = false)
    private Integer known = 0;

    @Column(name = "learning", nullable = false)
    private Integer learning = 0;

    @Column(name = "remain", nullable = false)
    private Integer remain = 0;

    @Column(name = "create_from")
    private String createFrom;

    @OneToMany(mappedBy = "flashcard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardItem> cards;
}

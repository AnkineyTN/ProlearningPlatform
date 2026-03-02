package com.cabybara.prolearningplatform.model.flashcard;

import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.FlashcardStatus;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.utils.SetChild;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flashcard")
public class Flashcard extends AbstractEntity implements SetChild {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private FlashcardStatus status = FlashcardStatus.NOT_COMPLETED;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy", nullable = false)
    @Builder.Default
    private Privacy privacy = Privacy.PRIVATE;

    @Column(name = "last_study")
    @Builder.Default
    private OffsetDateTime lastStudy = OffsetDateTime.now();

    @Column(name = "known", nullable = false)
    @Builder.Default
    private Integer known = 0;

    @Column(name = "learning", nullable = false)
    @Builder.Default
    private Integer learning = 0;

    @Column(name = "remain", nullable = false)
    @Builder.Default
    private Integer remain = 0;

    @Column(name = "create_method")
    @Builder.Default
    private CreationMethod create_method = CreationMethod.MANUAL;

    @OneToMany(mappedBy = "flashcard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardItem> cards;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_set", referencedColumnName = "id")
    @JsonIgnore
    @ToString.Exclude
    private Set set;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    @JsonIgnore
    @ToString.Exclude
    private User user;
}

package com.cabybara.prolearningplatform.model.note;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

import com.cabybara.prolearningplatform.enums.NoteMemberStatus;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.model.User;

@Entity
@Table(name = "note_members")
@Getter
@Setter
@NoArgsConstructor
public class NoteMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoteRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoteMemberStatus status = NoteMemberStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
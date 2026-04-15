package com.cabybara.prolearningplatform.model.flashcard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

import com.cabybara.prolearningplatform.enums.FlashcardMemberStatus;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.model.User;

@Entity
@Table(name = "flashcard_members")
@Getter
@Setter
@NoArgsConstructor
public class FlashcardMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoteRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FlashcardMemberStatus status = FlashcardMemberStatus.ACTIVE;

    @Column(name = "invite_token", unique = true)
    private String inviteToken;

    @Column(name = "invite_token_expires_at")
    private LocalDateTime inviteTokenExpiresAt;

    @Column(name = "invited_email")
    private String invitedEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}

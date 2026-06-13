package com.cabybara.prolearningplatform.dto.response.flashcard;

import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.FlashcardStatus;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public class FlashcardResponseDto {
    private String id;
    private String title;
    private String description;
    private FlashcardStatus status;
    private Privacy privacy;
    private OffsetDateTime lastStudy;
    private Integer known;
    private Integer learning;
    private Integer remain;

    private CreationMethod createMethod;

    private Long numCards;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private NoteRole userRole;
    private Boolean isFavorited;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatar;
}

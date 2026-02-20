package com.cabybara.prolearningplatform.dto.response.flashcard;

import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.FlashcardStatus;
import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Data
@SuperBuilder
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
}

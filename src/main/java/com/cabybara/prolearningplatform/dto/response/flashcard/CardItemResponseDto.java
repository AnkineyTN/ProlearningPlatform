package com.cabybara.prolearningplatform.dto.response.flashcard;

import com.cabybara.prolearningplatform.enums.CardStatus;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Data
@SuperBuilder
public class CardItemResponseDto {
    private Long id;
    private String frontCard;
    private String backCard;
    private String imageUrl;
    private CardStatus cardStatus;
    private OffsetDateTime nextReviewAt;
    private String topic;
}

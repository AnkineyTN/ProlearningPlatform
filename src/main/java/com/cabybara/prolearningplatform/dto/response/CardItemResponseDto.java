package com.cabybara.prolearningplatform.dto.response;

import com.cabybara.prolearningplatform.enums.CardStatus;
import lombok.Builder;
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
}

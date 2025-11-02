package com.cabybara.prolearningplatform.dto.request;

import com.cabybara.prolearningplatform.enums.CardStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardItemUpdatingRequestDto {
    private Long id;
    private String frontCard;
    private String backCard;
    private Long imageAssetId;
    private CardStatus cardStatus;
}

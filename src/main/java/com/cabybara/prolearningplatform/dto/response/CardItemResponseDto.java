package com.cabybara.prolearningplatform.dto.response;

import com.cabybara.prolearningplatform.enums.CardStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardItemResponseDto {
    private String frontCard;
    private String backCard;
    private String imageUrl;
    private CardStatus cardStatus;
}

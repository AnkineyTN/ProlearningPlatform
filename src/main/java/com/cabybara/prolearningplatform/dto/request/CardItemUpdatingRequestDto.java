package com.cabybara.prolearningplatform.dto.request;

import com.cabybara.prolearningplatform.enums.CardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardItemUpdatingRequestDto {
    @NotNull
    private Long id;

    private String frontCard;
    private String backCard;
    private Long imageAssetId;
    private CardStatus cardStatus;
}

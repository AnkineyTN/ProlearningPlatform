package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardItemReviewRequestDto {
    @NotNull
    private Long cardId;

    @NotNull
    private boolean isKnown;
}

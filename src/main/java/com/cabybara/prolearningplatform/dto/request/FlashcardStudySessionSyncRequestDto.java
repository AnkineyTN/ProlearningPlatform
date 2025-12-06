package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FlashcardStudySessionSyncRequestDto {
    @NotNull
    private List<CardItemReviewRequestDto> cardItemReviews;
}

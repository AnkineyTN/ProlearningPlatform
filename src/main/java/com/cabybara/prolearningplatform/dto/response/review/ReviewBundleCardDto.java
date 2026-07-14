package com.cabybara.prolearningplatform.dto.response.review;

public record ReviewBundleCardDto(
        Long id,
        String frontCard,
        String backCard
) {
    public interface Projection {
        Long getId();
        String getFrontCard();
        String getBackCard();
    }
}

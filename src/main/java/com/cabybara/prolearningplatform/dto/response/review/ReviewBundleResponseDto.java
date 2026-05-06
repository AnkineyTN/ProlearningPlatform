package com.cabybara.prolearningplatform.dto.response.review;

import java.time.OffsetDateTime;
import java.util.List;

public record ReviewBundleResponseDto(
        Long id,
        OffsetDateTime periodFrom,
        OffsetDateTime periodTo,
        int cardCount,
        List<ReviewBundleCardDto> cards
) {}

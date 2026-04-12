package com.cabybara.prolearningplatform.dto.response.review;

import java.time.OffsetDateTime;

public record ReviewBundleListItemDto(
        Long id,
        Long setId,
        OffsetDateTime periodFrom,
        OffsetDateTime periodTo,
        int cardCount
) {}

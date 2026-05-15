package com.cabybara.prolearningplatform.dto.response.social;

import java.time.OffsetDateTime;

public record SocialItemResponseDto(
        Long id,
        String type,
        String title,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Long ownerId,
        String ownerName,
        Long numCards,
        Long numQuestions,
        Long duration
) {}
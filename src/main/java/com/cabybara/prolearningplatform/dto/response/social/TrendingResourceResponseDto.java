package com.cabybara.prolearningplatform.dto.response.social;

import com.cabybara.prolearningplatform.enums.ContentType;

public record TrendingResourceResponseDto(
        int rank,
        Long id,
        Long setId,
        ContentType type,
        String title,
        String description,
        Long ownerId,
        String ownerName,
        long trendingScore,
        long viewCount,
        long sessionCount
) {}

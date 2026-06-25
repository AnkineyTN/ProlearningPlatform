package com.cabybara.prolearningplatform.dto.response.social;

public record TopCreatorResponseDto(
        int rank,
        Long userId,
        String fullName,
        String email,
        String avatarUrl,
        long totalResources,
        long newResourcesInPeriod
) {}

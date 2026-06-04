package com.cabybara.prolearningplatform.dto.response.social;

public record TopTopicResponseDto(
        int rank,
        String topic,
        long totalResources,
        long newResourcesInPeriod
) {}

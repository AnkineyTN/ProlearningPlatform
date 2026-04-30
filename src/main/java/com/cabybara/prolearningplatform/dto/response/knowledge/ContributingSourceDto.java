package com.cabybara.prolearningplatform.dto.response.knowledge;

import java.time.OffsetDateTime;

public record ContributingSourceDto(
        String sourceType,
        Long sourceId,
        Long analysisId,
        OffsetDateTime analyzedAt
) {}

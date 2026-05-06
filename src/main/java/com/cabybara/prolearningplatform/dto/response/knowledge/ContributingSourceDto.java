package com.cabybara.prolearningplatform.dto.response.knowledge;

import com.cabybara.prolearningplatform.enums.KnowledgeSourceType;

import java.time.OffsetDateTime;

public record ContributingSourceDto(
        KnowledgeSourceType knowledgeSourceType,
        Long sourceId,
        Long analysisId,
        OffsetDateTime analyzedAt
) {}

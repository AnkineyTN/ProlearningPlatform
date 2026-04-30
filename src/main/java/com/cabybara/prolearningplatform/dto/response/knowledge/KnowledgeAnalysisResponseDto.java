package com.cabybara.prolearningplatform.dto.response.knowledge;

import java.time.OffsetDateTime;
import java.util.List;

public record KnowledgeAnalysisResponseDto(
        Long id,
        String sourceType,
        Long sourceId,
        Long sessionRefId,
        List<TopicAccuracyDto> topicAccuracies,
        String strengths,
        String weaknesses,
        String improvements,
        OffsetDateTime createdAt,
        List<ContributingSourceDto> contributingSources
) {}

package com.cabybara.prolearningplatform.dto.response.knowledge;

import com.cabybara.prolearningplatform.enums.KnowledgeSourceType;

import java.time.OffsetDateTime;
import java.util.List;

public record KnowledgeAnalysisResponseDto(
        Long id,
        KnowledgeSourceType knowledgeSourceType,
        Long sourceId,
        Long sessionRefId,
        List<TopicAccuracyDto> topicAccuracies,
        String strengths,
        String weaknesses,
        String improvements,
        OffsetDateTime createdAt,
        List<ContributingSourceDto> contributingSources
) {}

package com.cabybara.prolearningplatform.service.knowledge;

import com.cabybara.prolearningplatform.dto.response.knowledge.TopicAccuracyDto;

import java.util.List;

public interface KnowledgeAccuracyService {
    List<TopicAccuracyDto> computeFromSession(Long sessionId);
    List<TopicAccuracyDto> computeFromAttempt(Long attemptId, Long examId);
}

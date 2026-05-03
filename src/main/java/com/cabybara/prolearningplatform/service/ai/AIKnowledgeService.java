package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.knowledge.KnowledgeAIResult;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentItem;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentResult;
import com.cabybara.prolearningplatform.dto.response.knowledge.TopicAccuracyDto;

import java.util.List;
import java.util.Map;

public interface AIKnowledgeService {
    List<TopicAssignmentResult> assignTopics(List<TopicAssignmentItem> items);
    Map<String, String> normalizeTopics(List<String> topics);
    KnowledgeAIResult analyzeKnowledge(List<TopicAccuracyDto> topicAccuracies);
}

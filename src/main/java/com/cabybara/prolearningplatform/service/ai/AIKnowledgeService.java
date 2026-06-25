package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.internal.knowledge.KnowledgeAIResult;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentItem;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentResult;
import com.cabybara.prolearningplatform.dto.response.knowledge.TopicAccuracyDto;

import java.util.List;
import java.util.Map;

public interface AIKnowledgeService {
    /** Sync call: resolves the current authenticated user's active LLM config. */
    List<TopicAssignmentResult> assignTopics(List<TopicAssignmentItem> items);

    /** Async/no-context call: caller supplies the resolved LLM config (e.g. by owning user id). */
    List<TopicAssignmentResult> assignTopics(List<TopicAssignmentItem> items, DecryptedLlmConfig cfg);

    Map<String, String> normalizeTopics(List<String> topics);
    KnowledgeAIResult analyzeKnowledge(List<TopicAccuracyDto> topicAccuracies);
}

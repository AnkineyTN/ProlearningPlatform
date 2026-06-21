package com.cabybara.prolearningplatform.service.knowledge.impl;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentItem;
import com.cabybara.prolearningplatform.dto.internal.knowledge.TopicAssignmentResult;
import com.cabybara.prolearningplatform.model.exam.Question;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.QuestionRepository;
import com.cabybara.prolearningplatform.service.ai.AIKnowledgeService;
import com.cabybara.prolearningplatform.service.knowledge.TopicAssignmentService;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicAssignmentServiceImpl implements TopicAssignmentService {

    private final CardItemRepository cardItemRepository;
    private final QuestionRepository questionRepository;
    private final AIKnowledgeService aiKnowledgeService;
    private final UserLlmConfigService userLlmConfigService;

    @Override
    @Transactional
    public int assignTopicsToFlashcard(Long flashcardId, Long userId) {
        List<CardItem> unassigned = cardItemRepository.findByFlashcardIdAndTopicIsNull(flashcardId);
        if (unassigned.isEmpty()) return 0;

        List<TopicAssignmentItem> items = unassigned.stream()
                .map(c -> new TopicAssignmentItem(c.getId(), c.getFrontCard() + " / " + c.getBackCard()))
                .toList();

        DecryptedLlmConfig cfg = userLlmConfigService.getDecryptedConfig(userId);
        List<TopicAssignmentResult> results = aiKnowledgeService.assignTopics(items, cfg);
        Map<Long, String> topicById = results.stream()
                .collect(Collectors.toMap(TopicAssignmentResult::id, TopicAssignmentResult::topic));

        unassigned.forEach(card -> {
            String topic = topicById.get(card.getId());
            if (topic != null) card.setTopic(topic);
        });

        cardItemRepository.saveAll(unassigned);
        log.info("Assigned topics to {} cards in flashcard {}", unassigned.size(), flashcardId);
        return unassigned.size();
    }

    @Override
    @Transactional
    public int assignTopicsToExam(Long examId, Long userId) {
        List<Question> unassigned = questionRepository.findByExamIdAndTopicIsNull(examId);
        if (unassigned.isEmpty()) return 0;

        List<TopicAssignmentItem> items = unassigned.stream()
                .map(q -> new TopicAssignmentItem(q.getId(), q.getContent()))
                .toList();

        DecryptedLlmConfig cfg = userLlmConfigService.getDecryptedConfig(userId);
        List<TopicAssignmentResult> results = aiKnowledgeService.assignTopics(items, cfg);
        Map<Long, String> topicById = results.stream()
                .collect(Collectors.toMap(TopicAssignmentResult::id, TopicAssignmentResult::topic));

        unassigned.forEach(q -> {
            String topic = topicById.get(q.getId());
            if (topic != null) q.setTopic(topic);
        });

        questionRepository.saveAll(unassigned);
        log.info("Assigned topics to {} questions in exam {}", unassigned.size(), examId);
        return unassigned.size();
    }
}

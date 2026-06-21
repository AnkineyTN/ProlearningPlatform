package com.cabybara.prolearningplatform.service.knowledge.impl;

import com.cabybara.prolearningplatform.service.knowledge.TopicAssignmentAsyncService;
import com.cabybara.prolearningplatform.service.knowledge.TopicAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicAssignmentAsyncServiceImpl implements TopicAssignmentAsyncService {

    private final TopicAssignmentService topicAssignmentService;

    @Override
    @Async("heavyTaskExecutor")
    public void assignTopicsToFlashcardAsync(Long flashcardId, Long userId) {
        try {
            topicAssignmentService.assignTopicsToFlashcard(flashcardId, userId);
        } catch (Exception e) {
            // Includes LlmNotConfiguredException when the user has no active LLM config —
            // topic assignment is skipped, the original create flow is unaffected.
            log.warn("Async topic assignment failed for flashcard {}: {}", flashcardId, e.getMessage());
        }
    }

    @Override
    @Async("heavyTaskExecutor")
    public void assignTopicsToExamAsync(Long examId, Long userId) {
        try {
            topicAssignmentService.assignTopicsToExam(examId, userId);
        } catch (Exception e) {
            log.warn("Async topic assignment failed for exam {}: {}", examId, e.getMessage());
        }
    }
}

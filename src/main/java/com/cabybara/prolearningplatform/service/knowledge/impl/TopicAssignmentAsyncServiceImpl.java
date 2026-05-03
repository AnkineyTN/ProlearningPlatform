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
    public void assignTopicsToFlashcardAsync(Long flashcardId) {
        try {
            topicAssignmentService.assignTopicsToFlashcard(flashcardId);
        } catch (Exception e) {
            log.error("Async topic assignment failed for flashcard {}: {}", flashcardId, e.getMessage());
        }
    }

    @Override
    @Async("heavyTaskExecutor")
    public void assignTopicsToExamAsync(Long examId) {
        try {
            topicAssignmentService.assignTopicsToExam(examId);
        } catch (Exception e) {
            log.error("Async topic assignment failed for exam {}: {}", examId, e.getMessage());
        }
    }
}

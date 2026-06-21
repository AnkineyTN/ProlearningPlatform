package com.cabybara.prolearningplatform.service.knowledge;

public interface TopicAssignmentAsyncService {
    void assignTopicsToFlashcardAsync(Long flashcardId, Long userId);
    void assignTopicsToExamAsync(Long examId, Long userId);
}

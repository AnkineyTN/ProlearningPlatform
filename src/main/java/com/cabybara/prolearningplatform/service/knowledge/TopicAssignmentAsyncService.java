package com.cabybara.prolearningplatform.service.knowledge;

public interface TopicAssignmentAsyncService {
    void assignTopicsToFlashcardAsync(Long flashcardId);
    void assignTopicsToExamAsync(Long examId);
}

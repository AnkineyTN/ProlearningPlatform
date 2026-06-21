package com.cabybara.prolearningplatform.service.knowledge;

public interface TopicAssignmentService {
    int assignTopicsToFlashcard(Long flashcardId, Long userId);
    int assignTopicsToExam(Long examId, Long userId);
}

package com.cabybara.prolearningplatform.service.knowledge;

public interface TopicAssignmentService {
    int assignTopicsToFlashcard(Long flashcardId);
    int assignTopicsToExam(Long examId);
}

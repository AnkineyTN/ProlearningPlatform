package com.cabybara.prolearningplatform.service.knowledge;

import com.cabybara.prolearningplatform.dto.response.knowledge.KnowledgeAnalysisResponseDto;

import java.util.List;

public interface KnowledgeAnalysisService {
    KnowledgeAnalysisResponseDto analyzeSession(Long setId, Long flashcardId, Long sessionId);
    KnowledgeAnalysisResponseDto analyzeAttempt(Long setId, Long examId, Long attemptId);
    KnowledgeAnalysisResponseDto analyzeSet(Long setId);

    List<KnowledgeAnalysisResponseDto> getAnalysesForFlashcard(Long setId, Long flashcardId);
    List<KnowledgeAnalysisResponseDto> getAnalysesForExam(Long setId, Long examId);
    List<KnowledgeAnalysisResponseDto> getAnalysesForSet(Long setId);

    int assignTopicsManualFlashcard(Long setId, Long flashcardId);
    int assignTopicsManualExam(Long setId, Long examId);
}

package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.knowledge.KnowledgeAnalysisResponseDto;
import com.cabybara.prolearningplatform.service.knowledge.KnowledgeAnalysisService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.cabybara.prolearningplatform.service.permission.annotation.AiRateLimit;

@RestController
@RequiredArgsConstructor
@Tag(name = "Knowledge Analysis")
public class KnowledgeAnalysisController {

    private final KnowledgeAnalysisService knowledgeAnalysisService;

    @PostMapping("/sets/{setId}/flashcards/{flashcardId}/topics/assign")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Manually trigger topic assignment for all cards in a flashcard")
    @AiRateLimit(type = "AI_INTERACTIVE")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> assignFlashcardTopics(
            @PathVariable Long setId,
            @PathVariable Long flashcardId) {
        int assigned = knowledgeAnalysisService.assignTopicsManualFlashcard(setId, flashcardId);
        return ResponseEntity.ok(ResponseUtil.success("Topics assigned", Map.of("assigned", assigned), null));
    }

    @PostMapping("/sets/{setId}/exams/{examId}/topics/assign")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Manually trigger topic assignment for all questions in an exam")
    @AiRateLimit(type = "AI_INTERACTIVE")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> assignExamTopics(
            @PathVariable Long setId,
            @PathVariable Long examId) {
        int assigned = knowledgeAnalysisService.assignTopicsManualExam(setId, examId);
        return ResponseEntity.ok(ResponseUtil.success("Topics assigned", Map.of("assigned", assigned), null));
    }



    @PostMapping("/sets/{setId}/flashcards/{flashcardId}/sessions/{sessionId}/analysis")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Analyze knowledge after a completed study session")
    @AiRateLimit(type = "AI_INTERACTIVE")
    public ResponseEntity<ApiResponse<KnowledgeAnalysisResponseDto>> analyzeSession(
            @PathVariable Long setId,
            @PathVariable Long flashcardId,
            @PathVariable Long sessionId) {
        KnowledgeAnalysisResponseDto dto = knowledgeAnalysisService.analyzeSession(setId, flashcardId, sessionId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Analysis created", dto, null));
    }

    @PostMapping("/sets/{setId}/exams/{examId}/attempts/{attemptId}/analysis")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Analyze knowledge after a submitted exam attempt")
    @AiRateLimit(type = "AI_INTERACTIVE")
    public ResponseEntity<ApiResponse<KnowledgeAnalysisResponseDto>> analyzeAttempt(
            @PathVariable Long setId,
            @PathVariable Long examId,
            @PathVariable Long attemptId) {
        KnowledgeAnalysisResponseDto dto = knowledgeAnalysisService.analyzeAttempt(setId, examId, attemptId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Analysis created", dto, null));
    }

    @PostMapping("/sets/{setId}/analysis")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Analyze knowledge across all resources in a set")
    @AiRateLimit(type = "AI_INTERACTIVE")
    public ResponseEntity<ApiResponse<KnowledgeAnalysisResponseDto>> analyzeSet(
            @PathVariable Long setId) {
        KnowledgeAnalysisResponseDto dto = knowledgeAnalysisService.analyzeSet(setId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Set analysis created", dto, null));
    }



    @GetMapping("/sets/{setId}/flashcards/{flashcardId}/analyses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get analysis history for a flashcard")
    public ResponseEntity<ApiResponse<List<KnowledgeAnalysisResponseDto>>> getFlashcardAnalyses(
            @PathVariable Long setId,
            @PathVariable Long flashcardId) {
        List<KnowledgeAnalysisResponseDto> list = knowledgeAnalysisService.getAnalysesForFlashcard(setId, flashcardId);
        return ResponseEntity.ok(ResponseUtil.success("OK", list, null));
    }

    @GetMapping("/sets/{setId}/exams/{examId}/analyses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get analysis history for an exam")
    public ResponseEntity<ApiResponse<List<KnowledgeAnalysisResponseDto>>> getExamAnalyses(
            @PathVariable Long setId,
            @PathVariable Long examId) {
        List<KnowledgeAnalysisResponseDto> list = knowledgeAnalysisService.getAnalysesForExam(setId, examId);
        return ResponseEntity.ok(ResponseUtil.success("OK", list, null));
    }

    @GetMapping("/sets/{setId}/analyses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get set-level analysis history")
    public ResponseEntity<ApiResponse<List<KnowledgeAnalysisResponseDto>>> getSetAnalyses(
            @PathVariable Long setId) {
        List<KnowledgeAnalysisResponseDto> list = knowledgeAnalysisService.getAnalysesForSet(setId);
        return ResponseEntity.ok(ResponseUtil.success("OK", list, null));
    }
}

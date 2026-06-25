package com.cabybara.prolearningplatform.service.knowledge.impl;

import com.cabybara.prolearningplatform.dto.internal.knowledge.KnowledgeAIResult;
import com.cabybara.prolearningplatform.dto.response.knowledge.ContributingSourceDto;
import com.cabybara.prolearningplatform.dto.response.knowledge.KnowledgeAnalysisResponseDto;
import com.cabybara.prolearningplatform.dto.response.knowledge.TopicAccuracyDto;
import com.cabybara.prolearningplatform.enums.ExamAttemptStatus;
import com.cabybara.prolearningplatform.enums.FlashcardStudySessionStatus;
import com.cabybara.prolearningplatform.enums.KnowledgeSourceType;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.exception.ResourceAlreadyExistsException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.exam.ExamAttempt;
import com.cabybara.prolearningplatform.model.flashcard_study_session.FlashcardStudySession;
import com.cabybara.prolearningplatform.model.knowledge.KnowledgeAnalysis;
import com.cabybara.prolearningplatform.repository.*;
import com.cabybara.prolearningplatform.service.ai.AIKnowledgeService;
import com.cabybara.prolearningplatform.service.knowledge.KnowledgeAccuracyService;
import com.cabybara.prolearningplatform.service.knowledge.KnowledgeAnalysisService;
import com.cabybara.prolearningplatform.service.knowledge.TopicAssignmentService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeAnalysisServiceImpl implements KnowledgeAnalysisService {

    private final AuthenticationContext authenticationContext;
    private final FlashcardStudySessionRepository sessionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final FlashcardRepository flashcardRepository;
    private final ExamRepository examRepository;
    private final KnowledgeAnalysisRepository analysisRepository;
    private final TopicAssignmentService topicAssignmentService;
    private final KnowledgeAccuracyService accuracyService;
    private final AIKnowledgeService aiKnowledgeService;


    @Override
    @Transactional
    public KnowledgeAnalysisResponseDto analyzeSession(Long setId, Long flashcardId, Long sessionId) {
        Long userId = authenticationContext.getCurrentUserId();

        FlashcardStudySession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found"));

        if (!session.getFlashcard().getId().equals(flashcardId)) {
            throw new BadRequestException("Session does not belong to the specified flashcard");
        }
        if (session.getStatus() != FlashcardStudySessionStatus.COMPLETED) {
            throw new BadRequestException("Session is not completed yet");
        }

        // 409 if already analyzed
        analysisRepository.findBySessionRefIdAndSourceType(sessionId, KnowledgeSourceType.FLASHCARD)
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException("Analysis already exists for this session");
                });

        // lazy topic assignment for MANUAL cards
        topicAssignmentService.assignTopicsToFlashcard(flashcardId, userId);

        List<TopicAccuracyDto> accuracies = accuracyService.computeFromSession(sessionId);
        if (accuracies.isEmpty()) {
            throw new BadRequestException("No topic data available for this session");
        }

        KnowledgeAIResult aiResult = aiKnowledgeService.analyzeKnowledge(accuracies);

        KnowledgeAnalysis saved = analysisRepository.save(KnowledgeAnalysis.builder()
                .userId(userId)
                .sourceType(KnowledgeSourceType.FLASHCARD)
                .sourceId(flashcardId)
                .sessionRefId(sessionId)
                .topicAccuracies(accuracies)
                .strengths(aiResult.strengths())
                .weaknesses(aiResult.weaknesses())
                .improvements(aiResult.improvements())
                .build());

        return toDto(saved);
    }

    @Override
    @Transactional
    public KnowledgeAnalysisResponseDto analyzeAttempt(Long setId, Long examId, Long attemptId) {
        Long userId = authenticationContext.getCurrentUserId();

        ExamAttempt attempt = examAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found"));

        if (!attempt.getExamId().equals(examId)) {
            throw new BadRequestException("Attempt does not belong to the specified exam");
        }
        if (attempt.getStatus() != ExamAttemptStatus.SUBMITTED) {
            throw new BadRequestException("Exam attempt is not submitted yet");
        }

        // 409 if already analyzed
        analysisRepository.findBySessionRefIdAndSourceType(attemptId, KnowledgeSourceType.EXAM)
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException("Analysis already exists for this attempt");
                });

        // lazy topic assignment for MANUAL questions
        topicAssignmentService.assignTopicsToExam(examId, userId);

        List<TopicAccuracyDto> accuracies = accuracyService.computeFromAttempt(attemptId, examId);
        if (accuracies.isEmpty()) {
            throw new BadRequestException("No topic data available for this attempt");
        }

        KnowledgeAIResult aiResult = aiKnowledgeService.analyzeKnowledge(accuracies);

        KnowledgeAnalysis saved = analysisRepository.save(KnowledgeAnalysis.builder()
                .userId(userId)
                .sourceType(KnowledgeSourceType.EXAM)
                .sourceId(examId)
                .sessionRefId(attemptId)
                .topicAccuracies(accuracies)
                .strengths(aiResult.strengths())
                .weaknesses(aiResult.weaknesses())
                .improvements(aiResult.improvements())
                .build());

        return toDto(saved);
    }

    @Override
    @Transactional
    public KnowledgeAnalysisResponseDto analyzeSet(Long setId) {
        Long userId = authenticationContext.getCurrentUserId();

        // Collect flashcard and exam IDs in the set
        List<Long> flashcardIds = flashcardRepository.findIdsBySetId(setId);
        List<Long> examIds      = examRepository.findIdsBySetId(setId);

        // Gather latest analysis per resource (batch fetch to avoid N+1)
        List<KnowledgeAnalysis> flashcardAnalyses = analysisRepository
                .findAllBySourceTypeAndSourceIdInOrderByCreatedAtDesc(KnowledgeSourceType.FLASHCARD, flashcardIds);
        List<KnowledgeAnalysis> examAnalyses = analysisRepository
                .findAllBySourceTypeAndSourceIdInOrderByCreatedAtDesc(KnowledgeSourceType.EXAM, examIds);

        Map<Long, KnowledgeAnalysis> latestBySourceId = new LinkedHashMap<>();
        flashcardAnalyses.forEach(a -> latestBySourceId.putIfAbsent(a.getSourceId(), a));
        examAnalyses.forEach(a -> latestBySourceId.putIfAbsent(a.getSourceId(), a));

        List<KnowledgeAnalysis> sources = new ArrayList<>(latestBySourceId.values());

        if (sources.isEmpty()) {
            throw new BadRequestException("No analyses found for resources in this set. Analyze individual flashcards or exams first.");
        }

        // Collect all raw topic strings for normalization
        List<String> allTopics = sources.stream()
                .flatMap(a -> a.getTopicAccuracies().stream().map(TopicAccuracyDto::topic))
                .distinct()
                .toList();

        Map<String, String> normalizationMap = aiKnowledgeService.normalizeTopics(allTopics);

        // Apply normalization and merge accuracies
        Map<String, List<Double>> mergedByTopic = new LinkedHashMap<>();
        for (KnowledgeAnalysis source : sources) {
            for (TopicAccuracyDto ta : source.getTopicAccuracies()) {
                String canonical = normalizationMap.getOrDefault(ta.topic(), ta.topic());
                mergedByTopic.computeIfAbsent(canonical, k -> new ArrayList<>()).add(ta.accuracy());
            }
        }

        List<TopicAccuracyDto> mergedAccuracies = mergedByTopic.entrySet().stream()
                .map(e -> new TopicAccuracyDto(e.getKey(),
                        e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0)))
                .toList();

        KnowledgeAIResult aiResult = aiKnowledgeService.analyzeKnowledge(mergedAccuracies);

        List<ContributingSourceDto> contributing = sources.stream()
                .map(s -> new ContributingSourceDto(s.getSourceType(), s.getSourceId(), s.getId(), s.getCreatedAt()))
                .toList();

        KnowledgeAnalysis saved = analysisRepository.save(KnowledgeAnalysis.builder()
                .userId(userId)
                .sourceType(KnowledgeSourceType.SET)
                .sourceId(setId)
                .sessionRefId(null)
                .topicAccuracies(mergedAccuracies)
                .strengths(aiResult.strengths())
                .weaknesses(aiResult.weaknesses())
                .improvements(aiResult.improvements())
                .contributingSources(contributing)
                .build());

        return toDto(saved);
    }



    @Override
    public List<KnowledgeAnalysisResponseDto> getAnalysesForFlashcard(Long setId, Long flashcardId) {
        flashcardRepository.findByIdAndSetId(flashcardId, setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found in set"));
        return analysisRepository.findAllBySourceTypeAndSourceIdOrderByCreatedAtDesc(KnowledgeSourceType.FLASHCARD, flashcardId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public List<KnowledgeAnalysisResponseDto> getAnalysesForExam(Long setId, Long examId) {
        examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        return analysisRepository.findAllBySourceTypeAndSourceIdOrderByCreatedAtDesc(KnowledgeSourceType.EXAM, examId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public List<KnowledgeAnalysisResponseDto> getAnalysesForSet(Long setId) {
        return analysisRepository.findAllBySourceTypeAndSourceIdOrderByCreatedAtDesc(KnowledgeSourceType.SET, setId)
                .stream().map(this::toDto).toList();
    }



    @Override
    public int assignTopicsManualFlashcard(Long setId, Long flashcardId) {
        Long userId = authenticationContext.getCurrentUserId();
        flashcardRepository.findByIdAndSetId(flashcardId, setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found in set"));
        return topicAssignmentService.assignTopicsToFlashcard(flashcardId, userId);
    }

    @Override
    public int assignTopicsManualExam(Long setId, Long examId) {
        Long userId = authenticationContext.getCurrentUserId();
        examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        return topicAssignmentService.assignTopicsToExam(examId, userId);
    }



    private KnowledgeAnalysisResponseDto toDto(KnowledgeAnalysis ka) {
        return new KnowledgeAnalysisResponseDto(
                ka.getId(),
                ka.getSourceType(),
                ka.getSourceId(),
                ka.getSessionRefId(),
                ka.getTopicAccuracies(),
                ka.getStrengths(),
                ka.getWeaknesses(),
                ka.getImprovements(),
                ka.getCreatedAt(),
                ka.getContributingSources()
        );
    }
}

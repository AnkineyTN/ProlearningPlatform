package com.cabybara.prolearningplatform.service.exam.impl;

import com.cabybara.prolearningplatform.dto.request.exam.AnswerSubmissionDto;
import com.cabybara.prolearningplatform.dto.request.exam.EssayGradingRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.SubmitExamRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.EssayGradingResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamAnswerResultDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamAttemptDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamAttemptResultDto;
import com.cabybara.prolearningplatform.enums.ExamAttemptStatus;
import com.cabybara.prolearningplatform.enums.QuestionType;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.exam.Exam;
import com.cabybara.prolearningplatform.model.exam.ExamAnswer;
import com.cabybara.prolearningplatform.model.exam.ExamAttempt;
import com.cabybara.prolearningplatform.model.exam.ExamQuestion;
import com.cabybara.prolearningplatform.repository.ExamAttemptRepository;
import com.cabybara.prolearningplatform.repository.ExamQuestionRepository;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.service.ai.AIExamService;
import com.cabybara.prolearningplatform.service.exam.ExamAttemptService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamAttemptServiceImpl implements ExamAttemptService {

    // ##################################################
    // #################  PREPARATION  ##################
    // ##################################################

    private final AuthenticationContext authenticationContext;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final AIExamService aiExamService;

    // ##################################################
    // #################  MAIN METHOD  ##################
    // ##################################################

    @Override
    @Transactional
    public ExamAttemptDto startAttempt(Long examId) {
        Long userId = authenticationContext.getCurrentUserId();

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        ExamAttempt attempt = new ExamAttempt();
        attempt.setExamId(examId);
        attempt.setUserId(userId);
        attempt.setStatus(ExamAttemptStatus.IN_PROGRESS);

        if (exam.getDuration() != null) {
            attempt.setDeadlineAt(LocalDateTime.now().plusSeconds(exam.getDuration()));
        }

        ExamAttempt saved = examAttemptRepository.save(attempt);
        return toAttemptDto(saved);
    }

    @Override
    @Transactional
    public ExamAttemptResultDto submitAttempt(Long examId, Long attemptId, SubmitExamRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();

        ExamAttempt attempt = examAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));

        if (!attempt.getExamId().equals(examId)) {
            throw new BadRequestException("Attempt does not belong to exam with id: " + examId);
        }

        if (attempt.getStatus() == ExamAttemptStatus.SUBMITTED) {
            throw new BadRequestException("Attempt has already been submitted");
        }

        if (attempt.getDeadlineAt() != null && LocalDateTime.now().isAfter(attempt.getDeadlineAt())) {
            throw new BadRequestException("Exam time has expired");
        }

        Map<Long, ExamQuestion> examQuestionsMap = examQuestionRepository.findAllByExamId(examId)
                .stream()
                .collect(Collectors.toMap(eq -> eq.getQuestion().getId(), eq -> eq));

        int totalPoints = examQuestionsMap.values().stream()
                .mapToInt(eq -> eq.getPoints() != null ? eq.getPoints() : 1)
                .sum();

        List<ExamAnswer> examAnswers = new ArrayList<>();

        for (AnswerSubmissionDto submission : request.answers()) {
            ExamQuestion examQuestion = examQuestionsMap.get(submission.questionId());
            if (examQuestion == null) {
                continue;
            }

            ExamAnswer answer = new ExamAnswer();
            answer.setAttempt(attempt);
            answer.setQuestionId(submission.questionId());
            answer.setSelectedOptionId(submission.selectedOptionId());
            answer.setEssayAnswer(submission.essayAnswer());

            QuestionType type = examQuestion.getQuestion().getType();
            if (type == QuestionType.ESSAY) {
                gradeEssayAnswer(answer, attempt, examQuestion, submission);
            } else {
                gradeObjectiveAnswer(answer, examQuestion, submission.selectedOptionId());
            }

            examAnswers.add(answer);
        }

        Double totalEarnedPoints = examAnswers.stream()
                .mapToDouble(a -> a.getEarnedPoints() != null ? (a.getEarnedPoints() instanceof Double ? (Double) a.getEarnedPoints() : ((Number) a.getEarnedPoints()).doubleValue()) : 0.0)
                .sum();

        attempt.getAnswers().addAll(examAnswers);
        attempt.setStatus(ExamAttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setScore(totalEarnedPoints);
        attempt.setTotalPoints(totalPoints);

        ExamAttempt saved = examAttemptRepository.save(attempt);
        return toAttemptResultDto(saved, examQuestionsMap);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamAttemptDto> getAttemptHistory(Long examId) {
        Long userId = authenticationContext.getCurrentUserId();

        return examAttemptRepository
                .findAllByExamIdAndUserIdOrderByStartedAtDesc(examId, userId)
                .stream()
                .map(this::toAttemptDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExamAttemptResultDto getAttemptDetail(Long examId, Long attemptId) {
        Long userId = authenticationContext.getCurrentUserId();

        ExamAttempt attempt = examAttemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));

        if (!attempt.getExamId().equals(examId)) {
            throw new BadRequestException("Attempt does not belong to exam with id: " + examId);
        }

        Map<Long, ExamQuestion> examQuestionsMap = examQuestionRepository.findAllByExamId(examId)
                .stream()
                .collect(Collectors.toMap(eq -> eq.getQuestion().getId(), eq -> eq));

        return toAttemptResultDto(attempt, examQuestionsMap);
    }

    private void gradeEssayAnswer(ExamAnswer answer, ExamAttempt attempt, ExamQuestion examQuestion, AnswerSubmissionDto submission) {
        int maxPoints = examQuestion.getPoints() != null ? examQuestion.getPoints() : 1;

        EssayGradingRequestDto gradingRequest = new EssayGradingRequestDto(
                attempt.getId(),
                submission.questionId(),
                examQuestion.getQuestion().getContent(),
                examQuestion.getQuestion().getExpectedAnswer(),
                submission.essayAnswer(),
                maxPoints
        );

        EssayGradingResponseDto gradingResult = aiExamService.gradeEssay(gradingRequest);

        answer.setIsCorrect(null);
        answer.setEarnedPoints(gradingResult.score());
        answer.setFeedback(gradingResult.feedback());
    }

    private void gradeObjectiveAnswer(ExamAnswer answer, ExamQuestion examQuestion, Long selectedOptionId) {
        if (selectedOptionId == null) {
            answer.setIsCorrect(false);
            answer.setEarnedPoints(0.0);
            return;
        }

        boolean correct = examQuestion.getQuestion().getOptions().stream()
                .anyMatch(opt -> opt.getId().equals(selectedOptionId) && Boolean.TRUE.equals(opt.getIsCorrect()));

        answer.setIsCorrect(correct);
        answer.setEarnedPoints(correct ? (double) (examQuestion.getPoints() != null ? examQuestion.getPoints() : 1) : 0.0);
    }

    private ExamAttemptDto toAttemptDto(ExamAttempt attempt) {
        return new ExamAttemptDto(
                attempt.getId(),
                attempt.getExamId(),
                attempt.getStatus(),
                attempt.getStartedAt(),
                attempt.getDeadlineAt(),
                attempt.getSubmittedAt(),
                attempt.getScore(),
                attempt.getTotalPoints()
        );
    }

    private ExamAttemptResultDto toAttemptResultDto(ExamAttempt attempt, Map<Long, ExamQuestion> examQuestionsMap) {
        List<ExamAnswerResultDto> answerDtos = attempt.getAnswers().stream()
                .map(a -> {
                    ExamQuestion eq = examQuestionsMap.get(a.getQuestionId());
                    String expectedAnswer = (eq != null) ? eq.getQuestion().getExpectedAnswer() : null;
                    return new ExamAnswerResultDto(
                            a.getQuestionId(),
                            a.getSelectedOptionId(),
                            a.getEssayAnswer(),
                            a.getIsCorrect(),
                            expectedAnswer,
                            a.getEarnedPoints(),
                            a.getFeedback()
                    );
                })
                .toList();

        return new ExamAttemptResultDto(
                attempt.getId(),
                attempt.getExamId(),
                attempt.getStatus(),
                attempt.getStartedAt(),
                attempt.getDeadlineAt(),
                attempt.getSubmittedAt(),
                attempt.getScore(),
                attempt.getTotalPoints(),
                answerDtos
        );
    }
    
}

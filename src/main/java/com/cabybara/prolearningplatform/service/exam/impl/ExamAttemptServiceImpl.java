package com.cabybara.prolearningplatform.service.exam.impl;

import com.cabybara.prolearningplatform.dto.request.exam.AnswerSubmissionDto;
import com.cabybara.prolearningplatform.dto.request.exam.SubmitExamRequestDto;
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
import com.cabybara.prolearningplatform.model.exam.QuestionOption;
import com.cabybara.prolearningplatform.repository.ExamAttemptRepository;
import com.cabybara.prolearningplatform.repository.ExamQuestionRepository;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.service.exam.ExamAttemptService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamAttemptServiceImpl implements ExamAttemptService {

    private final AuthenticationContext authenticationContext;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAttemptRepository examAttemptRepository;

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

        // Load all questions for exam
        Map<Long, ExamQuestion> examQuestionsMap = new java.util.HashMap<>();
        for (ExamQuestion eq : examQuestionRepository.findAllByExamId(examId)) {
            examQuestionsMap.put(eq.getQuestion().getId(), eq);
        }

        int totalPoints = examQuestionsMap.values().stream()
                .mapToInt(eq -> eq.getPoints() != null ? eq.getPoints() : 1)
                .sum();

        List<ExamAnswer> examAnswers = new ArrayList<>();
        BigDecimal score = BigDecimal.ZERO;

        for (AnswerSubmissionDto submission : request.answers()) {
            ExamQuestion examQuestion = examQuestionsMap.get(submission.questionId());

            // Skip if question does not exist
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
                // Graded by AI service
                answer.setIsCorrect(null);
            } else {
                boolean correct = gradeObjectiveAnswer(examQuestion, submission.selectedOptionId());
                answer.setIsCorrect(correct);
                if (correct) {
                    int points = examQuestion.getPoints() != null ? examQuestion.getPoints() : 1;
                    score = score.add(BigDecimal.valueOf(points));
                }
            }

            examAnswers.add(answer);
        }

        attempt.getAnswers().addAll(examAnswers);
        attempt.setStatus(ExamAttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setScore(score);
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

    // Returns true if the selected option is marked correct for this question
    private boolean gradeObjectiveAnswer(ExamQuestion examQuestion, Long selectedOptionId) {
        if (selectedOptionId == null) {
            return false;
        }
        return examQuestion.getQuestion().getOptions().stream()
                .anyMatch(opt -> opt.getId().equals(selectedOptionId) && Boolean.TRUE.equals(opt.getIsCorrect()));
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
                            expectedAnswer
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

package com.cabybara.prolearningplatform.service.exam;

import com.cabybara.prolearningplatform.dto.request.exam.AnswerSubmissionDto;
import com.cabybara.prolearningplatform.dto.request.exam.SubmitExamRequestDto;
import com.cabybara.prolearningplatform.dto.helper.QuestionErrorStat;
import com.cabybara.prolearningplatform.dto.response.exam.ExamAttemptDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamAttemptResultDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionErrorStatDto;
import com.cabybara.prolearningplatform.enums.ExamAttemptStatus;
import com.cabybara.prolearningplatform.enums.QuestionType;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.exam.Exam;
import com.cabybara.prolearningplatform.model.exam.ExamAnswer;
import com.cabybara.prolearningplatform.model.exam.ExamAttempt;
import com.cabybara.prolearningplatform.model.exam.ExamQuestion;
import com.cabybara.prolearningplatform.model.exam.Question;
import com.cabybara.prolearningplatform.model.exam.QuestionOption;
import com.cabybara.prolearningplatform.repository.ExamAttemptRepository;
import com.cabybara.prolearningplatform.repository.ExamQuestionRepository;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.service.ai.AIExamService;
import com.cabybara.prolearningplatform.service.exam.impl.ExamAttemptServiceImpl;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamAttemptServiceImplTest {

    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private ExamRepository examRepository;
    @Mock
    private ExamQuestionRepository examQuestionRepository;
    @Mock
    private ExamAttemptRepository examAttemptRepository;
    @Mock
    private AIExamService aiExamService;

    @Test
    void startAttemptReturnsExamAttemptDto() {
        ExamAttemptServiceImpl service = new ExamAttemptServiceImpl(
                authenticationContext, examRepository, examQuestionRepository, examAttemptRepository, aiExamService);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);

        Exam exam = new Exam();
        exam.setId(1L);
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(examAttemptRepository.save(any(ExamAttempt.class))).thenAnswer(invocation -> {
            ExamAttempt attempt = invocation.getArgument(0);
            attempt.setId(10L);
            return attempt;
        });

        ExamAttemptDto result = service.startAttempt(1L);

        verify(examAttemptRepository).save(any(ExamAttempt.class));
        assertEquals(1L, result.examId());
        assertEquals(ExamAttemptStatus.IN_PROGRESS, result.status());
    }

    @Test
    void startAttemptWithDurationSetsDeadline() {
        ExamAttemptServiceImpl service = new ExamAttemptServiceImpl(
                authenticationContext, examRepository, examQuestionRepository, examAttemptRepository, aiExamService);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);

        Exam exam = new Exam();
        exam.setId(1L);
        exam.setDuration(3600L);
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(examAttemptRepository.save(any(ExamAttempt.class))).thenAnswer(invocation -> {
            ExamAttempt attempt = invocation.getArgument(0);
            attempt.setId(11L);
            return attempt;
        });

        ExamAttemptDto result = service.startAttempt(1L);

        assertNotNull(result.deadlineAt());
    }

    @Test
    void submitAttemptCalculatesScoreForObjectiveQuestions() {
        ExamAttemptServiceImpl service = new ExamAttemptServiceImpl(
                authenticationContext, examRepository, examQuestionRepository, examAttemptRepository, aiExamService);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);

        ExamAttempt attempt = new ExamAttempt();
        attempt.setId(1L);
        attempt.setExamId(1L);
        attempt.setUserId(1L);
        attempt.setStatus(ExamAttemptStatus.IN_PROGRESS);
        attempt.setAnswers(new ArrayList<>());
        when(examAttemptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(attempt));

        Question question = new Question();
        question.setId(100L);
        question.setType(QuestionType.MULTIPLE_CHOICE);

        QuestionOption correctOption = new QuestionOption();
        correctOption.setId(10L);
        correctOption.setIsCorrect(true);
        correctOption.setOptionText("Correct");
        question.setOptions(new ArrayList<>(List.of(correctOption)));

        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setQuestion(question);
        examQuestion.setPoints(1);

        when(examQuestionRepository.findAllByExamId(1L)).thenReturn(List.of(examQuestion));
        when(examAttemptRepository.save(any(ExamAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnswerSubmissionDto submission = new AnswerSubmissionDto(100L, 10L, null);

        SubmitExamRequestDto request = new SubmitExamRequestDto(List.of(submission));

        ExamAttemptResultDto result = service.submitAttempt(1L, 1L, request);

        assertEquals(ExamAttemptStatus.SUBMITTED, result.status());
        assertEquals(1, result.answers().size());
        assertEquals(true, result.answers().get(0).isCorrect());
    }

    @Test
    void submitAttemptAlreadySubmittedThrows() {
        ExamAttemptServiceImpl service = new ExamAttemptServiceImpl(
                authenticationContext, examRepository, examQuestionRepository, examAttemptRepository, aiExamService);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);

        ExamAttempt attempt = new ExamAttempt();
        attempt.setId(1L);
        attempt.setExamId(1L);
        attempt.setUserId(1L);
        attempt.setStatus(ExamAttemptStatus.SUBMITTED);
        when(examAttemptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(attempt));

        SubmitExamRequestDto request = new SubmitExamRequestDto(List.of());

        assertThrows(BadRequestException.class, () -> service.submitAttempt(1L, 1L, request));
    }

    @Test
    void getQuestionStatsReturnsErrorDistribution() {
        ExamAttemptServiceImpl service = new ExamAttemptServiceImpl(
                authenticationContext, examRepository, examQuestionRepository, examAttemptRepository, aiExamService);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);

        QuestionErrorStat stat1 = new QuestionErrorStat() {
            @Override
            public Long getQuestionId() { return 1L; }
            @Override
            public String getQuestionText() { return "Q1"; }
            @Override
            public Long getTotalAttempts() { return 10L; }
            @Override
            public Long getIncorrectCount() { return 3L; }
        };

        QuestionErrorStat stat2 = new QuestionErrorStat() {
            @Override
            public Long getQuestionId() { return 2L; }
            @Override
            public String getQuestionText() { return "Q2"; }
            @Override
            public Long getTotalAttempts() { return 10L; }
            @Override
            public Long getIncorrectCount() { return 5L; }
        };

        when(examAttemptRepository.findQuestionStats(eq(1L), eq(1L), anyDouble()))
                .thenReturn(List.of(stat1, stat2));

        List<QuestionErrorStatDto> stats = service.getQuestionStats(1L);

        assertEquals(2, stats.size());
        assertEquals(10, stats.get(0).totalAttempts());
        assertEquals(3, stats.get(0).incorrectCount());
    }
}

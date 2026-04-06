package com.cabybara.prolearningplatform.service.exam.impl;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamQuestionViewDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionListResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.ExamMapper;
import com.cabybara.prolearningplatform.model.exam.Exam;
import com.cabybara.prolearningplatform.model.exam.ExamQuestion;
import com.cabybara.prolearningplatform.model.exam.Question;
import com.cabybara.prolearningplatform.model.exam.QuestionOption;
import com.cabybara.prolearningplatform.repository.QuestionRepository;
import com.cabybara.prolearningplatform.repository.ExamQuestionRepository;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.service.exam.QuestionService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamMapper examMapper;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional
    @CacheEvict(value = "exam_questions", allEntries = true)
    public QuestionListResponseDto createQuestion(Long examId, List<CreateQuestionRequestDto> createQuestionRequestDtos) {
        Long userId = authenticationContext.getCurrentUserId();

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        AtomicReference<Integer> currentOrderIndex = new AtomicReference<>(examQuestionRepository.countByExamId(examId));

        List<Question> questions = new ArrayList<>();
        List<ExamQuestion> examQuestions = new ArrayList<>();

        createQuestionRequestDtos.forEach(dto -> {
            Question question = createQuestionFromDto(dto, userId);
            questions.add(question);

            ExamQuestion examQuestion = createExamQuestion(exam, question, currentOrderIndex.getAndSet(currentOrderIndex.get() + 1), dto);
            examQuestions.add(examQuestion);
        });

        questionRepository.saveAll(questions);
        examQuestionRepository.saveAll(examQuestions);

        return buildResponseDto(examQuestions);
    }

    @Override
    @Cacheable(value = "exam_questions", key = "'exam:' + #examId")
    public QuestionListResponseDto getQuestionsByExamId(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found");
        }

        List<ExamQuestion> examQuestions = examQuestionRepository.findAllByExamId(examId);

        List<QuestionResponseDto> questionResponseDtos = examQuestions.stream()
                .map(examMapper::toQuestionResponseDto)
                .toList();


        return QuestionListResponseDto.builder()
                .questions(questionResponseDtos)
                .build();
    }

    @Override
    @Cacheable(value = "question", key = "#questionId")
    public QuestionResponseDto getQuestionById(Long examId, Long questionId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found");
        }

        ExamQuestion examQuestion = examQuestionRepository.findByExamIdAndQuestionId(examId, questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found in this exam"));

        return examMapper.toQuestionResponseDto(examQuestion);
    }

    @Override
    @Transactional
    @CachePut(value = "question", key = "#questionId")
    @CacheEvict(value = "exam_questions", allEntries = true)
    public QuestionResponseDto updateQuestion(Long examId, Long questionId, UpdateQuestionRequestDto dto) {
        validateExamQuestionRelation(examId, questionId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        examMapper.updateQuestionFromDto(dto, question);

        if (dto.options() != null) {
            question.getOptions().clear();
            List<QuestionOption> newOptions = examMapper.toQuestionOptionList(dto.options());
            newOptions.forEach(option -> option.setQuestion(question));
            question.getOptions().addAll(newOptions);
        }

        Question updatedQuestion = questionRepository.save(question);
        return examMapper.toQuestionResponseDto(updatedQuestion);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"question", "exam_questions"}, allEntries = true, beforeInvocation = true)
    public void deleteQuestion(Long examId, Long questionId) {
        validateExamQuestionRelation(examId, questionId);

        examQuestionRepository.deleteByExamIdAndQuestionId(examId, questionId);
        questionRepository.deleteById(questionId);
    }

    @Override
    public List<ExamQuestionViewDto> getQuestionsForTaking(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found");
        }

        return examQuestionRepository.findAllByExamId(examId)
                .stream()
                .map(examMapper::toExamQuestionViewDto)
                .toList();
    }

    private void validateExamQuestionRelation(Long examId, Long questionId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found");
        }

        examQuestionRepository.findByExamIdAndQuestionId(examId, questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found in this exam"));
    }

    private Question createQuestionFromDto(CreateQuestionRequestDto dto, Long userId) {
        Question question = examMapper.toQuestion(dto);
        question.setCreatedBy(userId);

        if (dto.options() != null && !dto.options().isEmpty()) {
            List<QuestionOption> options = examMapper.toQuestionOptionList(dto.options());
            options.forEach(option -> option.setQuestion(question));
            question.setOptions(options);
        }

        return question;
    }

    private ExamQuestion createExamQuestion(Exam exam, Question question, int orderIndex, CreateQuestionRequestDto dto) {
        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExam(exam);
        examQuestion.setQuestion(question);
        examQuestion.setOrderIndex(orderIndex);
        examQuestion.setPoints(dto.point());
        return examQuestion;
    }

    private QuestionListResponseDto buildResponseDto(List<ExamQuestion> examQuestions) {
        List<QuestionResponseDto> questionResponseDtos = examQuestions.stream()
                .map(examMapper::toQuestionResponseDto)
                .toList();

        return QuestionListResponseDto.builder()
                .questions(questionResponseDtos)
                .build();
    }
}

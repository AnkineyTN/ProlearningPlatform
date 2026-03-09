package com.cabybara.prolearningplatform.service.exam.impl;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionListResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.ExamMapper;
import com.cabybara.prolearningplatform.model.exam.Question;
import com.cabybara.prolearningplatform.model.exam.QuestionOption;
import com.cabybara.prolearningplatform.model.exam.Quiz;
import com.cabybara.prolearningplatform.model.exam.QuizQuestion;
import com.cabybara.prolearningplatform.repository.QuestionRepository;
import com.cabybara.prolearningplatform.repository.QuizQuestionRepository;
import com.cabybara.prolearningplatform.repository.QuizRepository;
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

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ExamMapper examMapper;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional
    @CacheEvict(value = "quiz_questions", allEntries = true)
    public QuestionListResponseDto createQuestion(Long quizId, List<CreateQuestionRequestDto> createQuestionRequestDtos) {
        Long userId = authenticationContext.getCurrentUserId();

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        AtomicReference<Integer> currentOrderIndex = new AtomicReference<>(quizQuestionRepository.countByQuizId(quizId));

        List<Question> questions = new ArrayList<>();
        List<QuizQuestion> quizQuestions = new ArrayList<>();

        createQuestionRequestDtos.forEach(dto -> {
            Question question = createQuestionFromDto(dto, userId);
            questions.add(question);

            QuizQuestion quizQuestion = createQuizQuestion(quiz, question, currentOrderIndex.getAndSet(currentOrderIndex.get() + 1), dto);
            quizQuestions.add(quizQuestion);
        });

        questionRepository.saveAll(questions);
        quizQuestionRepository.saveAll(quizQuestions);

        return buildResponseDto(questions);
    }

    @Override
    @Cacheable(value = "quiz_questions", key = "'quiz:' + #quizId")
    public QuestionListResponseDto getQuestionsByQuizId(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found");
        }

        List<Question> questions = questionRepository.findAllByQuizId(quizId);

        List<QuestionResponseDto> questionResponseDtos = questions.stream()
                .map(examMapper::toQuestionResponseDto)
                .toList();

        return QuestionListResponseDto.builder()
                .questions(questionResponseDtos)
                .build();
    }

    @Override
    @Cacheable(value = "question", key = "#questionId")
    public QuestionResponseDto getQuestionById(Long quizId, Long questionId) {
        validateQuizQuestionRelation(quizId, questionId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        return examMapper.toQuestionResponseDto(question);
    }

    @Override
    @Transactional
    @CachePut(value = "question", key = "#questionId")
    @CacheEvict(value = "quiz_questions", allEntries = true)
    public QuestionResponseDto updateQuestion(Long quizId, Long questionId, UpdateQuestionRequestDto dto) {
        validateQuizQuestionRelation(quizId, questionId);

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
    @CacheEvict(value = {"question", "quiz_questions"}, allEntries = true, beforeInvocation = true)
    public void deleteQuestion(Long quizId, Long questionId) {
        validateQuizQuestionRelation(quizId, questionId);

        quizQuestionRepository.deleteByQuizIdAndQuestionId(quizId, questionId);
        questionRepository.deleteById(questionId);
    }

    private void validateQuizQuestionRelation(Long quizId, Long questionId) {
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found");
        }

        quizQuestionRepository.findByQuizIdAndQuestionId(quizId, questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found in this quiz"));
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

    private QuizQuestion createQuizQuestion(Quiz quiz, Question question, int orderIndex, CreateQuestionRequestDto dto) {
        QuizQuestion quizQuestion = new QuizQuestion();
        quizQuestion.setQuiz(quiz);
        quizQuestion.setQuestion(question);
        quizQuestion.setOrderIndex(orderIndex);
        quizQuestion.setPoints(dto.point());
        return quizQuestion;
    }

    private QuestionListResponseDto buildResponseDto(List<Question> questions) {
        List<QuestionResponseDto> questionResponseDtos = questions.stream()
                .map(examMapper::toQuestionResponseDto)
                .toList();

        return QuestionListResponseDto.builder()
                .questions(questionResponseDtos)
                .build();
    }
}

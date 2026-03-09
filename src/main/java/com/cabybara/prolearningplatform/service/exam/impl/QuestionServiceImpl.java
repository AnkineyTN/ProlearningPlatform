package com.cabybara.prolearningplatform.service.exam.impl;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateQuestionRequestDto;
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
import org.springframework.stereotype.Service;

import java.util.List;

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
    public QuestionResponseDto createQuestion(Long quizId, CreateQuestionRequestDto dto) {
        Long userId = authenticationContext.getCurrentUserId();

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        Question question = examMapper.toQuestion(dto);
        question.setCreatedBy(userId);

        if (dto.options() != null && !dto.options().isEmpty()) {
            List<QuestionOption> options = examMapper.toQuestionOptionList(dto.options());
            options.forEach(option -> option.setQuestion(question));
            question.setOptions(options);
        }

        Question savedQuestion = questionRepository.save(question);

        QuizQuestion quizQuestion = new QuizQuestion();
        quizQuestion.setQuiz(quiz);
        quizQuestion.setQuestion(savedQuestion);
        quizQuestion.setOrderIndex(quizQuestionRepository.countByQuizId(quizId) + 1);
        quizQuestion.setPoints(dto.point());
        quizQuestionRepository.save(quizQuestion);

        return examMapper.toQuestionResponseDto(savedQuestion);
    }

    @Override
    public List<QuestionResponseDto> getQuestionsByQuizId(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found");
        }

        List<Question> questions = questionRepository.findAllByQuizId(quizId);

        return questions.stream()
                .map(examMapper::toQuestionResponseDto)
                .toList();
    }

    @Override
    public QuestionResponseDto getQuestionById(Long quizId, Long questionId) {
        validateQuizQuestionRelation(quizId, questionId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        return examMapper.toQuestionResponseDto(question);
    }

    @Override
    @Transactional
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
}

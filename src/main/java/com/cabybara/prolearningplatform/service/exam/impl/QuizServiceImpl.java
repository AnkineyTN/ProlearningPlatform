package com.cabybara.prolearningplatform.service.exam.impl;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuizRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateQuizRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuizResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceAlreadyExistsException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.ExamMapper;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.exam.Question;
import com.cabybara.prolearningplatform.model.exam.QuestionOption;
import com.cabybara.prolearningplatform.model.exam.Quiz;
import com.cabybara.prolearningplatform.repository.QuizRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.service.exam.QuizService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final AuthenticationContext authenticationContext;
    private final QuizRepository quizRepository;
    private final SetRepository setRepository;
    private final ExamMapper examMapper;

    @Override
//    @CacheEvict(value = "set_quízzes", key = "'set' + #setId")
    public QuizResponseDto createQuiz(Long setId, CreateQuizRequestDto createQuizRequestDto) {
        Long userId = authenticationContext.getCurrentUserId();

        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("set not found"));

        if (quizRepository.existsByTitleAndSet(createQuizRequestDto.title(), set)) {
            throw new ResourceAlreadyExistsException("Exam has been existed");
        }

        Quiz quiz = examMapper.toQuiz(createQuizRequestDto);
        quiz.setCreatedBy(userId);
        quiz.setSet(set);

        return examMapper.toQuizResponseDto(quizRepository.save(quiz));
    }

    @Override
//    @Cacheable(value = "set_quízzes", key = "'set' + #setId")
    public List<QuizResponseDto> getQuiz(Long setId, Pageable pageable) {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("set not found"));

        return quizRepository.findAllBySet(set, pageable).stream()
                .map(examMapper::toQuizResponseDto)
                .toList();
    }

    @Override
//    @Cacheable(value = "quiz", key = "#quizId")
    public QuizResponseDto getQuiz(Long setId, Long quizId) {
        Quiz quiz = quizRepository.findBySetIdAndId(setId, quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find quiz with id: " + quizId));

        return examMapper.toQuizResponseDto(quiz);
    }

    @Override
    public QuizResponseDto updateQuiz(Long setId, Long quizId, UpdateQuizRequestDto updateQuizRequestDto) {
        if (!quizRepository.existsBySetIdAndId(setId, quizId)) {
            throw new ResourceNotFoundException("Cannot find quiz with id: " + quizId + " in set with id: " + setId);
        }

        Quiz quiz = quizRepository.findById(quizId)
                        .orElseThrow(() -> new ResourceNotFoundException("Cannot find quiz with id: " + quizId));

        examMapper.updateQuizFromDto(updateQuizRequestDto, quiz);

        return examMapper.toQuizResponseDto(quizRepository.save(quiz));
    }

    @Override
    public void deleteQuiz(Long setId, Long quizId) {
        Quiz quiz = quizRepository.findBySetIdAndId(setId, quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find quiz with id: " + quizId));

        quizRepository.delete(quiz);
    }
}

package com.cabybara.prolearningplatform.service.exam.impl;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuizRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuizResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceAlreadyExistsException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.QuizMapper;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.exam.Quiz;
import com.cabybara.prolearningplatform.repository.QuizRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.service.exam.QuizService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final AuthenticationContext authenticationContext;
    private final QuizRepository quizRepository;
    private final SetRepository setRepository;
    private final QuizMapper quizMapper;

    @Override
    public QuizResponseDto createQuiz(Long setId, CreateQuizRequestDto createQuizRequestDto) {
        Long userId = authenticationContext.getCurrentUserId();

        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("set not found"));

        if (quizRepository.existsByTitleAndSet(createQuizRequestDto.title(), set)) {
            throw new ResourceAlreadyExistsException("Exam has been existed");
        }

        Quiz quiz = quizMapper.toQuiz(createQuizRequestDto);
        quiz.setCreatedBy(userId);
        quiz.setSet(set);

        return quizMapper.toQuizResponseDto(quizRepository.save(quiz));
    }

    @Override
    public List<QuizResponseDto> getQuizzes(Long setId, Pageable pageable) {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("set not found"));

        return quizRepository.findAllBySet(set, pageable).stream()
                .map(quizMapper::toQuizResponseDto)
                .toList();
    }
}

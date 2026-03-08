package com.cabybara.prolearningplatform.service.exam;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuizRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuizResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizService {
    QuizResponseDto createQuiz(Long setId, CreateQuizRequestDto createQuizRequestDto);

    List<QuizResponseDto> getQuizzes(Long setId, Pageable pageable);
}

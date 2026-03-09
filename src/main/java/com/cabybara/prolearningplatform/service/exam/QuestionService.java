package com.cabybara.prolearningplatform.service.exam;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionResponseDto;

import java.util.List;

public interface QuestionService {
    QuestionResponseDto createQuestion(Long quizId, CreateQuestionRequestDto dto);

    List<QuestionResponseDto> getQuestionsByQuizId(Long quizId);

    QuestionResponseDto getQuestionById(Long quizId, Long questionId);

    QuestionResponseDto updateQuestion(Long quizId, Long questionId, UpdateQuestionRequestDto dto);

    void deleteQuestion(Long quizId, Long questionId);
}

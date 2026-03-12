package com.cabybara.prolearningplatform.service.exam;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuizRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateQuizRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuizResponseDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizService {
    QuizResponseDto createQuiz(Long setId, CreateQuizRequestDto createQuizRequestDto);

    List<QuizResponseDto> getQuiz(Long setId, Pageable pageable);

    QuizResponseDto getQuiz(Long setId, Long quizId);

    QuizResponseDto updateQuiz(Long setId, Long quizId, @Valid UpdateQuizRequestDto updateQuizRequestDto);

    void deleteQuiz(Long setId, Long quizId);

    GenerateExamByAIResponseDto generateExamByFiles(GenerateExamByFileRequestDto request);

    GenerateExamByAIResponseDto generateExamByNotes(GenerateExamByNoteRequestDto request);
}

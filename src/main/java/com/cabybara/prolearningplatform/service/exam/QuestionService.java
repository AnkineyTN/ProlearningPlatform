package com.cabybara.prolearningplatform.service.exam;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamQuestionViewDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionListResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionResponseDto;

import java.util.List;

public interface QuestionService {
    QuestionListResponseDto createQuestion(Long examId, List<CreateQuestionRequestDto> dto);

    // Management view (creator/admin)
    QuestionListResponseDto getQuestionsByExamId(Long examId);

    QuestionResponseDto getQuestionById(Long examId, Long questionId);

    QuestionResponseDto updateQuestion(Long examId, Long questionId, UpdateQuestionRequestDto dto);

    void deleteQuestion(Long examId, Long questionId);

    List<ExamQuestionViewDto> getQuestionsForTaking(Long examId);
}

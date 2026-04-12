package com.cabybara.prolearningplatform.service.exam;

import com.cabybara.prolearningplatform.dto.request.exam.CreateExamFromReviewRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.CreateExamRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateExamRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.enums.Privacy;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExamService {

    ExamResponseDto createExam(Long setId, CreateExamRequestDto createExamRequestDto);

    ExamResponseDto createExamFromReview(CreateExamFromReviewRequestDto dto, Long setId);

    Page<ExamResponseDto> getExam(Long setId, String q, Privacy privacy, Pageable pageable);

    ExamResponseDto getExam(Long setId, Long ExamId);

    ExamResponseDto updateExam(Long setId, Long ExamId, @Valid UpdateExamRequestDto updateExamRequestDto);

    void deleteExam(Long setId, Long ExamId);

    GenerateExamByAIResponseDto generateExamByNotes(GenerateExamByNoteRequestDto request);

}

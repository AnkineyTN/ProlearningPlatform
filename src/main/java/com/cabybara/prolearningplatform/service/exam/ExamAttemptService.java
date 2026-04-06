package com.cabybara.prolearningplatform.service.exam;

import com.cabybara.prolearningplatform.dto.request.exam.SubmitExamRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamAttemptDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamAttemptResultDto;

import java.util.List;

public interface ExamAttemptService {

    ExamAttemptDto startAttempt(Long examId);

    ExamAttemptResultDto submitAttempt(Long examId, Long attemptId, SubmitExamRequestDto request);

    List<ExamAttemptDto> getAttemptHistory(Long examId);

    ExamAttemptResultDto getAttemptDetail(Long examId, Long attemptId);

}

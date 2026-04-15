package com.cabybara.prolearningplatform.service.exam;

import com.cabybara.prolearningplatform.dto.request.exam.CreateExamFromReviewRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.CreateExamRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateExamRequestDto;
import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.note.AcceptByTokenResponse;
import com.cabybara.prolearningplatform.dto.response.share.InviteResultResponse;
import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.Privacy;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExamService {

    ExamResponseDto createExam(Long setId, CreateExamRequestDto createExamRequestDto);

    ExamResponseDto createExamFromReview(CreateExamFromReviewRequestDto dto, Long setId);

    ExamResponseDto createReviewExamFromQuestions(Long examId, List<Long> questionIds);

    Page<ExamResponseDto> getExam(Long setId, String q, Privacy privacy, CreationMethod createMethod, Pageable pageable);

    ExamResponseDto getExam(Long setId, Long ExamId);

    ExamResponseDto updateExam(Long setId, Long ExamId, @Valid UpdateExamRequestDto updateExamRequestDto);

    void deleteExam(Long setId, Long ExamId);

    GenerateExamByAIResponseDto generateExamByNotes(GenerateExamByNoteRequestDto request);

    List<InviteResultResponse> inviteMembers(Long setId, Long examId, InviteMemberRequest request);

    void acceptInvite(Long examId);

    void declineInvite(Long examId);

    void removeMember(Long examId, Long targetUserId);

    AcceptByTokenResponse acceptByToken(String token);

}

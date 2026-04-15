package com.cabybara.prolearningplatform.dto.response.share;

import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InviteResultResponse {
    private Long userId;
    private String email;
    private boolean success;
    private String error; // null nếu success

    public static InviteResultResponse success(
            InviteMemberRequest.InviteTarget target, Long resolvedUserId) {
        return new InviteResultResponse(
            resolvedUserId, target.getEmail(), true, null);
    }

    public static InviteResultResponse failed(
            InviteMemberRequest.InviteTarget target, String reason) {
        return new InviteResultResponse(
            target.getUserId(), target.getEmail(), false, reason);
    }

    @Data
    @AllArgsConstructor
    public static class PendingInviteFlashcardResponse {
        private Long flashcardId;
        private String flashcardTitle;
        private String role;
        private Long setId;
        private LocalDateTime invitedAt;
    }

    @Data
    @AllArgsConstructor
    public static class PendingInviteExamResponse {
        private Long examId;
        private String examTitle;
        private String role;
        private Long setId;
        private LocalDateTime invitedAt;
    }
}
package com.cabybara.prolearningplatform.dto.response.share;

import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;

import lombok.AllArgsConstructor;
import lombok.Data;

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
}
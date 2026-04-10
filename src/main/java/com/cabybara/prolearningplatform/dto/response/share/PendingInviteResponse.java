package com.cabybara.prolearningplatform.dto.response.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PendingInviteResponse {
    private Long noteId;
    private String noteTitle;
    private String role;
    private LocalDateTime invitedAt;
}
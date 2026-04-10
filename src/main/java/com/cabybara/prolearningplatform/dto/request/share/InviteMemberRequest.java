package com.cabybara.prolearningplatform.dto.request.share;

import lombok.Data;
import java.util.List;

import com.cabybara.prolearningplatform.enums.NoteRole;

@Data
public class InviteMemberRequest {
    private List<InviteTarget> targets;
    private NoteRole role; // tất cả targets cùng role

    @Data
    public static class InviteTarget {
        private Long userId;   // nếu tìm được qua search
        private String email;  // fallback nhập email
    }
}
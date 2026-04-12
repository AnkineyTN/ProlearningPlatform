package com.cabybara.prolearningplatform.dto.response.share;

import com.cabybara.prolearningplatform.enums.NoteRole;

import lombok.Data;

@Data
public class UpdateMemberRoleRequest {
    private NoteRole role;
}
package com.cabybara.prolearningplatform.dto.response.note;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AcceptByTokenResponse {
    private boolean success;
    private String error;
    private Long noteId;
}

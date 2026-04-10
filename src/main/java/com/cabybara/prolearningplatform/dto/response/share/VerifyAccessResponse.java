package com.cabybara.prolearningplatform.dto.response.share;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifyAccessResponse {
    private Long userId;
    private String role;
}
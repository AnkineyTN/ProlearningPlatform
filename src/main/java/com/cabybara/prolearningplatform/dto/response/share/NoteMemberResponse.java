package com.cabybara.prolearningplatform.dto.response.share;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NoteMemberResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String status;
}

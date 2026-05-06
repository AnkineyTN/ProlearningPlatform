package com.cabybara.prolearningplatform.dto.response.user;

import lombok.*;

@Value
@Builder
public class RegisterResponseDto {
    Long id;
    String firstName;
    String lastName;
    String email;
}

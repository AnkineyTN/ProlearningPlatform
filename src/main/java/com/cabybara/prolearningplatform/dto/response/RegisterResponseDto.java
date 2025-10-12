package com.cabybara.prolearningplatform.dto.response;

import lombok.*;

@Value
@Builder
public class RegisterResponseDto {
    Long id;
    String firstName;
    String lastName;
    String email;
}

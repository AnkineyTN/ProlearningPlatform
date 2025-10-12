package com.cabybara.prolearningplatform.dto.response;

import lombok.*;

@Value
@Builder
public class LoginResponseDto {
    UserResponseDto userResponseDto;
    String accessToken;
}

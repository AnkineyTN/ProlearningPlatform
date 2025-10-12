package com.cabybara.prolearningplatform.dto;

import com.cabybara.prolearningplatform.enums.Role;
import lombok.*;

import java.util.Set;

@Value
@Builder
public class LoginResponseDto {
    UserResponseDto userResponseDto;
    String accessToken;
}

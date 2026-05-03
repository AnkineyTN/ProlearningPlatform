package com.cabybara.prolearningplatform.dto.response.user;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RefreshTokenResponseDto {
    String accessToken;
    String refreshToken;
}

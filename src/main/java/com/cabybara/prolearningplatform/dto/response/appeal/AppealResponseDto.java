package com.cabybara.prolearningplatform.dto.response.appeal;

import com.cabybara.prolearningplatform.enums.AppealStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class AppealResponseDto {
    private Long id;
    private Long userId;
    private String email;
    private String reason;
    private AppealStatus status;
    private String adminNote;
    private OffsetDateTime createdAt;
    private OffsetDateTime resolvedAt;
}

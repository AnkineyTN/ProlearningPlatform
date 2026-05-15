package com.cabybara.prolearningplatform.dto.request.admin;

import com.cabybara.prolearningplatform.enums.AppealStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminAppealReviewDto {
    @NotNull(message = "status is required")
    private AppealStatus status;

    private String adminNote;
}

package com.cabybara.prolearningplatform.dto.request.payment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a PayOS payment link")
public record CreatePaymentLinkRequestDto(
        @Schema(
                description = "Client platform requesting the payment link",
                allowableValues = {"web", "mobile"},
                example = "web",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String platform
) {}

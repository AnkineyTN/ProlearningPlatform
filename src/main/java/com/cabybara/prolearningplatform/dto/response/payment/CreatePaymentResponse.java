package com.cabybara.prolearningplatform.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePaymentResponse {
    private Long orderCode;
    private String checkoutUrl;
}

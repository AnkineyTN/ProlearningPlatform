package com.cabybara.prolearningplatform.dto.response.payment;

import com.cabybara.prolearningplatform.enums.AccountType;
import com.cabybara.prolearningplatform.enums.PaymentOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentStatusResponse {
    private Long orderCode;
    private PaymentOrderStatus status;
    private AccountType accountType;
}

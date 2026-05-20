package com.cabybara.prolearningplatform.dto.internal.payos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayOSPaymentData {
    private long orderCode;
    private String status;
    private String checkoutUrl;
    private String paymentLinkId;
    private int amount;
}
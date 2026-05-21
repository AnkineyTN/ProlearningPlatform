package com.cabybara.prolearningplatform.dto.internal.payos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayOSCreateRequest {
    private long orderCode;
    private int amount;
    private String description;
    private String returnUrl;
    private String cancelUrl;
    private String signature;
    private String buyerName;
    private String buyerEmail;
    private int expiredAt;
}
package com.cabybara.prolearningplatform.dto.internal.payos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayOSApiResponse {
    private String code;
    private String desc;
    private PayOSPaymentData data;
}
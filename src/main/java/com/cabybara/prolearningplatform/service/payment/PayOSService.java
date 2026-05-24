package com.cabybara.prolearningplatform.service.payment;

import com.cabybara.prolearningplatform.dto.request.payment.PayOSWebhookPayload;
import com.cabybara.prolearningplatform.dto.response.payment.CreatePaymentResponse;
import com.cabybara.prolearningplatform.dto.response.payment.PaymentStatusResponse;

public interface PayOSService {
    CreatePaymentResponse createPaymentLink(Long userId, String platform);
    void cancelPaymentLink(Long userId, long orderCode);
    PaymentStatusResponse getPaymentStatus(Long userId, long orderCode);
    void handleWebhookEvent(PayOSWebhookPayload payload);
}

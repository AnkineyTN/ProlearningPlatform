package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.configuration.PayOSProperties;
import com.cabybara.prolearningplatform.dto.request.payment.PayOSWebhookPayload;
import com.cabybara.prolearningplatform.service.payment.PayOSService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.HmacSHA256Util;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class PayOSWebhookController {

    private final PayOSProperties payOSProperties;
    private final PayOSService payOSService;

    @PostMapping("/payos")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(@RequestBody PayOSWebhookPayload payload) {
        if (!isSignatureValid(payload)) {
            log.warn("PayOS webhook: invalid signature for orderCode={}", safeOrderCode(payload));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseUtil.error("Invalid signature", null, null));
        }

        try {
            payOSService.handleWebhookEvent(payload);
        } catch (Exception e) {
            log.error("PayOS webhook: failed to handle event for orderCode={}", safeOrderCode(payload), e);
        }

        return ResponseEntity.ok(ResponseUtil.success("OK", null, null));
    }

    private boolean isSignatureValid(PayOSWebhookPayload payload) {
        if (payload.getData() == null || payload.getSignature() == null) return false;
        PayOSWebhookPayload.Data d = payload.getData();
        String rawData = "accountNumber=" + safe(d.getAccountNumber())
                + "&amount=" + d.getAmount()
                + "&code=" + safe(d.getCode())
                + "&counterAccountBankId=" + safe(d.getCounterAccountBankId())
                + "&counterAccountBankName=" + safe(d.getCounterAccountBankName())
                + "&counterAccountName=" + safe(d.getCounterAccountName())
                + "&counterAccountNumber=" + safe(d.getCounterAccountNumber())
                + "&currency=" + safe(d.getCurrency())
                + "&desc=" + safe(d.getDesc())
                + "&description=" + safe(d.getDescription())
                + "&orderCode=" + d.getOrderCode()
                + "&paymentLinkId=" + safe(d.getPaymentLinkId())
                + "&reference=" + safe(d.getReference())
                + "&transactionDateTime=" + safe(d.getTransactionDateTime())
                + "&virtualAccountName=" + safe(d.getVirtualAccountName())
                + "&virtualAccountNumber=" + safe(d.getVirtualAccountNumber());
        String expected = HmacSHA256Util.sign(rawData, payOSProperties.getChecksumKey());
        return expected.equals(payload.getSignature());
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private long safeOrderCode(PayOSWebhookPayload payload) {
        return payload.getData() != null ? payload.getData().getOrderCode() : -1;
    }


}

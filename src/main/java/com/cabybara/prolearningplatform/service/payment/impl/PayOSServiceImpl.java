package com.cabybara.prolearningplatform.service.payment.impl;

import com.cabybara.prolearningplatform.configuration.PayOSProperties;
import com.cabybara.prolearningplatform.dto.internal.payos.PayOSApiResponse;
import com.cabybara.prolearningplatform.dto.request.payment.PayOSWebhookPayload;
import com.cabybara.prolearningplatform.dto.response.payment.CreatePaymentResponse;
import com.cabybara.prolearningplatform.dto.response.payment.PaymentStatusResponse;
import com.cabybara.prolearningplatform.enums.PaymentOrderStatus;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.payment.PaymentOrder;
import com.cabybara.prolearningplatform.model.payment.PaymentTransaction;
import com.cabybara.prolearningplatform.repository.PaymentOrderRepository;
import com.cabybara.prolearningplatform.repository.PaymentTransactionRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.payment.PayOSService;
import com.cabybara.prolearningplatform.service.payment.SubscriptionService;
import com.cabybara.prolearningplatform.utils.HmacSHA256Util;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cabybara.prolearningplatform.dto.internal.payos.PayOSCreateRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSServiceImpl implements PayOSService {

    private static final int PRO_UPGRADE_AMOUNT = 10000;
    private static final String UPGRADE_DESCRIPTION = "PROLEARNING - PRO TIER";
    private static final int LINK_TTL_SECONDS = 15 * 60;

    private final PayOSProperties payOSProperties;
    private final RestHttpClientUtil restHttpClientUtil;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Override
    public CreatePaymentResponse createPaymentLink(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        cancelExistingPendingOrders(userId);

        long orderCode = System.currentTimeMillis();
        int expiredAt = (int) (Instant.now().getEpochSecond() + LINK_TTL_SECONDS);
        String signature = HmacSHA256Util.sign(buildCreateLinkSignatureData(orderCode), payOSProperties.getChecksumKey());

        PayOSCreateRequest requestBody = new PayOSCreateRequest();
        requestBody.setOrderCode(orderCode);
        requestBody.setAmount(PRO_UPGRADE_AMOUNT);
        requestBody.setDescription(UPGRADE_DESCRIPTION);
        requestBody.setReturnUrl(payOSProperties.getReturnUrl());
        requestBody.setCancelUrl(payOSProperties.getCancelUrl());
        requestBody.setSignature(signature);
        requestBody.setBuyerName(user.getFirstName() + " " + user.getLastName());
        requestBody.setBuyerEmail(user.getEmail());
        requestBody.setExpiredAt(expiredAt);

        PayOSApiResponse apiResponse = restHttpClientUtil.post(
                payOSProperties.getBaseUrl() + "/v2/payment-requests",
                requestBody,
                buildPayOSHeaders(),
                PayOSApiResponse.class
        );

        if (!"00".equals(apiResponse.getCode())) {
            throw new BadRequestException("PayOS error: " + apiResponse.getDesc());
        }

        paymentOrderRepository.save(PaymentOrder.builder()
                .orderCode(orderCode)
                .user(user)
                .amount(PRO_UPGRADE_AMOUNT)
                .checkoutUrl(apiResponse.getData().getCheckoutUrl())
                .paymentLinkId(apiResponse.getData().getPaymentLinkId())
                .expiredAt(Instant.ofEpochSecond(expiredAt))
                .build());

        return new CreatePaymentResponse(orderCode, apiResponse.getData().getCheckoutUrl());
    }

    @Override
    public void cancelPaymentLink(Long userId, long orderCode) {
        PaymentOrder order = paymentOrderRepository.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderCode));

        if (order.getStatus() != PaymentOrderStatus.PENDING) {
            throw new BadRequestException("Cannot cancel order with status: " + order.getStatus());
        }

        restHttpClientUtil.post(
                payOSProperties.getBaseUrl() + "/v2/payment-requests/" + orderCode + "/cancel",
                Map.of("cancellationReason", "User requested cancellation"),
                buildPayOSHeaders(),
                Object.class
        );

        order.setStatus(PaymentOrderStatus.CANCELLED);
        paymentOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(Long userId, long orderCode) {
        PaymentOrder order = paymentOrderRepository.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderCode));
        return new PaymentStatusResponse(orderCode, order.getStatus(), order.getUser().getAccountType());
    }

    @Override
    @Transactional
    public void handleWebhookEvent(PayOSWebhookPayload payload) {
        PayOSWebhookPayload.Data data = payload.getData();

        PaymentOrder order = paymentOrderRepository.findByOrderCode(data.getOrderCode()).orElse(null);
        if (order == null) {
            log.warn("PayOS webhook: unknown orderCode={}", data.getOrderCode());
            return;
        }
        if (order.getStatus() == PaymentOrderStatus.PAID || order.getStatus() == PaymentOrderStatus.CANCELLED) {
            return;
        }

        if (payload.isSuccess() && "00".equals(payload.getCode())) {
            processSuccessfulPayment(order, data);
            log.info("PayOS webhook: payment processed for orderCode={}", data.getOrderCode());
        } else {
            PaymentOrderStatus newStatus = "CANCELLED".equals(data.getCode())
                    ? PaymentOrderStatus.CANCELLED
                    : PaymentOrderStatus.EXPIRED;
            order.setStatus(newStatus);
            paymentOrderRepository.save(order);
            log.info("PayOS webhook: order {} marked as {}", data.getOrderCode(), newStatus);
        }
    }

    private void processSuccessfulPayment(PaymentOrder order, PayOSWebhookPayload.Data data) {
        order.setStatus(PaymentOrderStatus.PAID);
        order.setPaymentLinkId(data.getPaymentLinkId());
        paymentOrderRepository.save(order);

        paymentTransactionRepository.save(PaymentTransaction.builder()
                .orderCode(data.getOrderCode())
                .user(order.getUser())
                .amount(data.getAmount())
                .reference(data.getReference())
                .transactionDateTime(data.getTransactionDateTime())
                .accountNumber(data.getAccountNumber())
                .build());

        subscriptionService.activatePro(order.getUser().getId());
    }

    private void cancelExistingPendingOrders(Long userId) {
        List<PaymentOrder> pending = paymentOrderRepository.findByUser_IdAndStatus(userId, PaymentOrderStatus.PENDING);
        if (pending.isEmpty()) return;

        for (PaymentOrder order : pending) {
            try {
                restHttpClientUtil.post(
                        payOSProperties.getBaseUrl() + "/v2/payment-requests/" + order.getOrderCode() + "/cancel",
                        Map.of("cancellationReason", "Superseded by new payment request"),
                        buildPayOSHeaders(),
                        Object.class
                );
            } catch (Exception e) {
                log.warn("Failed to cancel PayOS link for orderCode={}: {}", order.getOrderCode(), e.getMessage());
            }
            order.setStatus(PaymentOrderStatus.CANCELLED);
        }
        paymentOrderRepository.saveAll(pending);
    }

    private String buildCreateLinkSignatureData(long orderCode) {
        return "amount=" + PRO_UPGRADE_AMOUNT
                + "&cancelUrl=" + payOSProperties.getCancelUrl()
                + "&description=" + UPGRADE_DESCRIPTION
                + "&orderCode=" + orderCode
                + "&returnUrl=" + payOSProperties.getReturnUrl();
    }

    private HttpHeaders buildPayOSHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", payOSProperties.getClientId());
        headers.set("x-api-key", payOSProperties.getApiKey());
        return headers;
    }
}

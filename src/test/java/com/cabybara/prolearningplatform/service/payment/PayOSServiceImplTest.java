package com.cabybara.prolearningplatform.service.payment;

import com.cabybara.prolearningplatform.configuration.PayOSProperties;
import com.cabybara.prolearningplatform.dto.internal.payos.PayOSApiResponse;
import com.cabybara.prolearningplatform.dto.internal.payos.PayOSCreateRequest;
import com.cabybara.prolearningplatform.dto.internal.payos.PayOSPaymentData;
import com.cabybara.prolearningplatform.dto.request.payment.PayOSWebhookPayload;
import com.cabybara.prolearningplatform.dto.response.payment.CreatePaymentResponse;
import com.cabybara.prolearningplatform.enums.PaymentOrderStatus;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.payment.PaymentOrder;
import com.cabybara.prolearningplatform.repository.PaymentOrderRepository;
import com.cabybara.prolearningplatform.repository.PaymentTransactionRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.payment.impl.PayOSServiceImpl;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayOSServiceImplTest {

    @Mock
    private PayOSProperties payOSProperties;

    @Mock
    private RestHttpClientUtil restHttpClientUtil;

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Test
    void createPaymentLinkCancelsOldPendingOrders() {
        PayOSServiceImpl payOSService = new PayOSServiceImpl(
                payOSProperties, restHttpClientUtil, paymentOrderRepository,
                paymentTransactionRepository, userRepository, subscriptionService);

        User user = TestFixtures.user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PaymentOrder oldOrder = PaymentOrder.builder()
                .orderCode(111L).user(user).amount(10000).status(PaymentOrderStatus.PENDING).build();
        List<PaymentOrder> oldOrders = List.of(oldOrder);
        when(paymentOrderRepository.findByUser_IdAndStatus(1L, PaymentOrderStatus.PENDING))
                .thenReturn(oldOrders);

        when(payOSProperties.getChecksumKey()).thenReturn("checksum-key");
        when(payOSProperties.getBaseUrl()).thenReturn("https://payos.example.com");
        when(payOSProperties.getReturnUrlWeb()).thenReturn("https://app.example.com/return");
        when(payOSProperties.getCancelUrlWeb()).thenReturn("https://app.example.com/cancel");
        when(payOSProperties.getClientId()).thenReturn("client-id");
        when(payOSProperties.getApiKey()).thenReturn("api-key");

        PayOSApiResponse apiResponse = new PayOSApiResponse();
        apiResponse.setCode("00");
        PayOSPaymentData pd = new PayOSPaymentData();
        pd.setCheckoutUrl("https://checkout.example.com");
        pd.setPaymentLinkId("plink-123");
        apiResponse.setData(pd);

        when(restHttpClientUtil.post(anyString(), any(Map.class), any(HttpHeaders.class), eq(Object.class)))
                .thenReturn(null);
        when(restHttpClientUtil.post(anyString(), any(PayOSCreateRequest.class), any(HttpHeaders.class), eq(PayOSApiResponse.class)))
                .thenReturn(apiResponse);

        CreatePaymentResponse response = payOSService.createPaymentLink(1L, "web");

        verify(paymentOrderRepository).saveAll(oldOrders);
        verify(paymentOrderRepository, atLeastOnce()).save(any(PaymentOrder.class));
        assertEquals(PaymentOrderStatus.CANCELLED, oldOrder.getStatus());
        assertNotNull(response);
    }

    @Test
    void handleWebhookPaidActivatesPro() {
        PayOSServiceImpl payOSService = new PayOSServiceImpl(
                payOSProperties, restHttpClientUtil, paymentOrderRepository,
                paymentTransactionRepository, userRepository, subscriptionService);

        User user = TestFixtures.user(1L);
        PaymentOrder order = PaymentOrder.builder()
                .orderCode(123L).user(user).status(PaymentOrderStatus.PENDING).build();
        when(paymentOrderRepository.findByOrderCode(123L)).thenReturn(Optional.of(order));

        PayOSWebhookPayload payload = new PayOSWebhookPayload();
        payload.setCode("00");
        payload.setSuccess(true);
        PayOSWebhookPayload.Data data = new PayOSWebhookPayload.Data();
        data.setOrderCode(123L);
        data.setPaymentLinkId("plink-123");
        data.setAmount(10000);
        payload.setData(data);

        payOSService.handleWebhookEvent(payload);

        assertEquals(PaymentOrderStatus.PAID, order.getStatus());
        verify(subscriptionService).activatePro(1L);
    }

    @Test
    void handleWebhookAlreadyPaidIsIdempotent() {
        PayOSServiceImpl payOSService = new PayOSServiceImpl(
                payOSProperties, restHttpClientUtil, paymentOrderRepository,
                paymentTransactionRepository, userRepository, subscriptionService);

        User user = TestFixtures.user(1L);
        PaymentOrder order = PaymentOrder.builder()
                .orderCode(123L).user(user).status(PaymentOrderStatus.PAID).build();
        when(paymentOrderRepository.findByOrderCode(123L)).thenReturn(Optional.of(order));

        PayOSWebhookPayload payload = new PayOSWebhookPayload();
        payload.setCode("00");
        payload.setSuccess(true);
        PayOSWebhookPayload.Data data = new PayOSWebhookPayload.Data();
        data.setOrderCode(123L);
        payload.setData(data);

        payOSService.handleWebhookEvent(payload);

        verify(subscriptionService, never()).activatePro(any());
    }

    @Test
    void cancelPaymentLinkMarksCancelled() {
        PayOSServiceImpl payOSService = new PayOSServiceImpl(
                payOSProperties, restHttpClientUtil, paymentOrderRepository,
                paymentTransactionRepository, userRepository, subscriptionService);

        User user = TestFixtures.user(1L);
        PaymentOrder order = PaymentOrder.builder()
                .orderCode(456L).user(user).status(PaymentOrderStatus.PENDING).build();
        when(paymentOrderRepository.findByOrderCodeAndUserId(456L, 1L)).thenReturn(Optional.of(order));

        when(payOSProperties.getBaseUrl()).thenReturn("https://payos.example.com");
        when(payOSProperties.getClientId()).thenReturn("client-id");
        when(payOSProperties.getApiKey()).thenReturn("api-key");

        when(restHttpClientUtil.post(anyString(), any(Map.class), any(HttpHeaders.class), eq(Object.class)))
                .thenReturn(null);

        payOSService.cancelPaymentLink(1L, 456L);

        assertEquals(PaymentOrderStatus.CANCELLED, order.getStatus());
        verify(paymentOrderRepository).save(order);
    }
}

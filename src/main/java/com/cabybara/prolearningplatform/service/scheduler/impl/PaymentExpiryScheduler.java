package com.cabybara.prolearningplatform.service.scheduler.impl;

import com.cabybara.prolearningplatform.enums.PaymentOrderStatus;
import com.cabybara.prolearningplatform.repository.PaymentOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentExpiryScheduler {
    private static final String SCHEDULER_TIME_ZONE = "Asia/Ho_Chi_Minh";

    private final PaymentOrderRepository paymentOrderRepository;

    @Transactional
    @Scheduled(cron = "${schedule.payment-expired-cleanup.cron:0 * * * * *}", zone = SCHEDULER_TIME_ZONE)
    public void expirePendingOrders() {
        int count = paymentOrderRepository.expirePendingOrders(
                PaymentOrderStatus.PENDING, PaymentOrderStatus.EXPIRED, Instant.now());
        if (count > 0) {
            log.info("Expired {} pending payment orders", count);
        }
    }
}

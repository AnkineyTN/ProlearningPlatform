package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.PaymentOrderStatus;
import com.cabybara.prolearningplatform.model.payment.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByOrderCode(Long orderCode);
    Optional<PaymentOrder> findByOrderCodeAndUserId(Long orderCode, Long userId);

    List<PaymentOrder> findByUser_IdAndStatus(Long userId, PaymentOrderStatus status);

    @Modifying
    @Query("UPDATE PaymentOrder o SET o.status = :expired WHERE o.status = :pending AND o.expiredAt < :now")
    int expirePendingOrders(@Param("pending") PaymentOrderStatus pendingStatus,
                            @Param("expired") PaymentOrderStatus expiredStatus,
                            @Param("now") Instant now);
}

package com.cabybara.prolearningplatform.model.payment;

import com.cabybara.prolearningplatform.enums.PaymentOrderStatus;
import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "payment_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrder extends AbstractEntity {

    @Column(name = "order_code", unique = true, nullable = false)
    private Long orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentOrderStatus status = PaymentOrderStatus.PENDING;

    @Column(name = "checkout_url", columnDefinition = "TEXT")
    private String checkoutUrl;

    @Column(name = "payment_link_id", length = 100)
    private String paymentLinkId;

    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;
}

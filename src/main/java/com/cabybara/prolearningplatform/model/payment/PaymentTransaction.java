package com.cabybara.prolearningplatform.model.payment;

import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction extends AbstractEntity {

    @Column(name = "order_code", nullable = false)
    private Long orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer amount;

    @Column(length = 100)
    private String reference;

    @Column(name = "transaction_date_time", length = 50)
    private String transactionDateTime;

    @Column(name = "account_number", length = 50)
    private String accountNumber;
}

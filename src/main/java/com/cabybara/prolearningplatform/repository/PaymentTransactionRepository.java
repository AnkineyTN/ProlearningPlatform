package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.payment.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
}

package com.example.demo.repository;

import com.example.demo.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long id);
    Page<Payment> findAllByDeletedFalse(Pageable pageable);
    Page<Payment> findByOrderIdContainingAndDeletedFalse(String orderId, Pageable pageable);
    Optional<Payment> findByVnpTransactionNo(String vnpTransactionNo);
    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);
}

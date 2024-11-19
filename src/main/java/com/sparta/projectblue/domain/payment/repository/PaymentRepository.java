package com.sparta.projectblue.domain.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.projectblue.domain.common.enums.PaymentStatus;
import com.sparta.projectblue.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByPaymentKey(String paymentKey);

    Optional<Payment> findByReservationIdAndStatus(Long reservationId, PaymentStatus status);

    Optional<Payment> findByReservationId(Long reservationId);
}

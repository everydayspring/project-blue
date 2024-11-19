package com.sparta.projectblue.domain.payment.service;

import java.time.OffsetDateTime;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.projectblue.aop.annotation.PaymentLogstash;
import com.sparta.projectblue.domain.common.exception.PaymentException;
import com.sparta.projectblue.domain.email.service.EmailCreateService;
import com.sparta.projectblue.domain.payment.entity.Payment;
import com.sparta.projectblue.domain.payment.repository.PaymentRepository;
import com.sparta.projectblue.domain.reservation.entity.Reservation;
import com.sparta.projectblue.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavePaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final EmailCreateService emailCreateService;

    @PaymentLogstash
    @Transactional
    public void savePayment(JSONObject jsonObject) {

        String orderId = (String) jsonObject.get("orderId");

        Long reservationId = Long.parseLong(orderId.substring(23));

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() -> new IllegalArgumentException("예매 정보를 찾을 수 없습니다"));

        OffsetDateTime approvedAt = OffsetDateTime.parse((String) jsonObject.get("approvedAt"));

        Payment payment =
                paymentRepository
                        .findByOrderId(orderId)
                        .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다"));

        payment.addPaymentInfo(
                (String) jsonObject.get("paymentKey"),
                (String) jsonObject.get("type"),
                (String) jsonObject.get("method"),
                (Long) jsonObject.get("suppliedAmount"),
                (Long) jsonObject.get("vat"),
                approvedAt.toLocalDateTime(),
                (Long) jsonObject.get("totalAmount"));

        emailCreateService.sendPaymentEmail(payment.getUserId(), payment);

        reservation.addPaymentId(payment.getId());

        reservation.resCompleted();
    }
}

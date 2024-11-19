package com.sparta.projectblue.domain.reservation.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sparta.projectblue.domain.common.enums.PaymentStatus;
import com.sparta.projectblue.domain.payment.entity.Payment;
import com.sparta.projectblue.domain.payment.repository.PaymentRepository;
import com.sparta.projectblue.domain.payment.service.PaymentService;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.reservedseat.entity.ReservedSeat;
import com.sparta.projectblue.domain.reservedseat.repository.ReservedSeatRepository;
import com.sparta.projectblue.domain.sse.service.NotificationService;

@Service
public class ReservationAsyncService {

    private final ReservedSeatRepository reservedSeatRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final PerformanceRepository performanceRepository;

    public ReservationAsyncService(
            ReservedSeatRepository reservedSeatRepository,
            PaymentRepository paymentRepository,
            PaymentService paymentService,
            NotificationService notificationService,
            PerformanceRepository performanceRepository) {
        this.reservedSeatRepository = reservedSeatRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
        this.performanceRepository = performanceRepository;
    }

    @Async("mailExecutor")
    public void deleteReservationSeats(Long reservationId) {
        List<ReservedSeat> reservedSeats =
                reservedSeatRepository.findByReservationId(reservationId);
        reservedSeatRepository.deleteAll(reservedSeats);
    }

    @Async("mailExecutor")
    public void deleteReservationPayment(Long paymentId) throws IOException {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (Objects.nonNull(payment) && !payment.getStatus().equals(PaymentStatus.CANCELED)) {
            paymentService.cancelPayment(payment.getPaymentKey(), "예매를 취소합니다");
        }
    }

    @Async("mailExecutor")
    public void deleteReservationSlack(String userName, Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId).orElse(null);
        if (Objects.nonNull(performance)) {
            String title = "[티켓_예매취소완료]";
            String message =
                    String.format("%s 고객님, %s 공연의 예약이 취소 되었습니다.", userName, performance.getTitle());
            notificationService.notify(userName, title, message);
        }
    }
}

package com.sparta.projectblue.domain.email.service;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.sparta.projectblue.domain.payment.entity.Payment;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.reservation.dto.CreateReservationResponseDto;
import com.sparta.projectblue.domain.reservation.entity.Reservation;
import com.sparta.projectblue.domain.reservation.repository.ReservationRepository;
import com.sparta.projectblue.domain.round.entity.Round;
import com.sparta.projectblue.domain.round.repository.RoundRepository;
import com.sparta.projectblue.domain.user.entity.User;
import com.sparta.projectblue.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailCreateService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PerformanceRepository performanceRepository;
    private final RoundRepository roundRepository;
    private final ReservationRepository reservationRepository;

    // 예매 시 메일 발송
    public void sendReservationEmail(Long userId, CreateReservationResponseDto responseDto) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        String subject =
                String.format(
                        "%s님, %s 예매가 완료되었습니다.", user.getName(), responseDto.getPerformanceTitle());

        StringBuilder text = new StringBuilder();
        text.append(wrapWithH3("이름 : " + responseDto.getPerformanceTitle()))
                .append(
                        wrapWithH3(
                                "일시 : "
                                        + responseDto
                                                .getRoundDate()
                                                .format(
                                                        DateTimeFormatter.ofPattern(
                                                                "yyyy-MM-dd HH:mm"))))
                .append(wrapWithH3("좌석 : " + responseDto.getSeats()))
                .append(wrapWithH3("총 가격 : " + responseDto.getPrice()))
                .append("결제까지 진행하셔야 예매가 확정됩니다.");

        emailService.sendMail(user.getEmail(), subject, text.toString());
    }

    // 결제 시 메일 발송
    public void sendPaymentEmail(Long userId, Payment payment) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Performance performance =
                performanceRepository
                        .findById(payment.getPerformanceId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공연입니다."));

        Reservation reservation =
                reservationRepository
                        .findById(payment.getReservationId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        Round round =
                roundRepository
                        .findById(reservation.getRoundId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회차입니다."));

        String subject =
                String.format("%s님의 %s 결제가 완료되었습니다.", user.getName(), performance.getTitle());

        StringBuilder text = new StringBuilder();
        text.append(wrapWithH3("이름 : " + performance.getTitle()))
                .append(
                        wrapWithH3(
                                "일시 : "
                                        + round.getDate()
                                                .format(
                                                        DateTimeFormatter.ofPattern(
                                                                "yyyy-MM-dd HH:mm"))))
                .append(wrapWithH3("총 가격 : " + payment.getOriginAmount()))
                .append(wrapWithH3("결제시간 : " + payment.getApprovedAt()))
                .append("이용해주셔서 감사합니다. 즐거운 시간되시길 바랍니다.");

        emailService.sendMail(user.getEmail(), subject, text.toString());
    }

    private String wrapWithH3(String content) {
        return "<h3>" + content + "</h3>";
    }
}

package com.sparta.projectblue.aop;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.sparta.projectblue.domain.common.exception.PaymentException;
import com.sparta.projectblue.domain.coupon.dto.CreateCouponResponseDto;
import com.sparta.projectblue.domain.payment.entity.Payment;
import com.sparta.projectblue.domain.payment.repository.PaymentRepository;
import com.sparta.projectblue.domain.reservation.dto.CreateReservationResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogstashAspect {

    private final PaymentRepository paymentRepository;

    @Pointcut("@annotation(com.sparta.projectblue.aop.annotation.ReservationLogstash)")
    private void reservationLog() {}

    @Pointcut("@annotation(com.sparta.projectblue.aop.annotation.PaymentLogstash)")
    private void paymentLog() {}

    @Pointcut("@annotation(com.sparta.projectblue.aop.annotation.CouponLogstash)")
    private void couponLog() {}

    @Around("reservationLog()")
    public Object reservationLogstash(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            log.error(
                    "ReservationEvent: 예매 실패 - 메서드: {}, 이유: {}",
                    joinPoint.getSignature().getName(),
                    e.getMessage());
            throw e;
        }

        // 예매 완료
        // 패턴 매칭을 적용한 코드
        if (result instanceof CreateReservationResponseDto reservation) {
            log.info(
                    "ReservationEvent: 예매 완료 - 예매 ID: {}, 공연명: {}, 날짜: {}, 좌석: {}, 총 가격: {}, 예약상태: {}",
                    reservation.getId(),
                    reservation.getPerformanceTitle(),
                    reservation.getRoundDate(),
                    reservation.getSeats(),
                    reservation.getPrice(),
                    reservation.getStatus());
        }

        // 예매 취소
        else if ("delete".equals(joinPoint.getSignature().getName())) {
            Object[] args = joinPoint.getArgs();
            Long reservationId = (Long) args[0];
            log.info("ReservationEvent: 예매 취소 - 유저 ID: {}", reservationId);
        } else {
            log.warn("ReservationEvent: 예상치 못한 결과 형식 - {}", result);
        }

        return result;
    }

    @Around("paymentLog()")
    public Object paymentLogstash(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            log.error(
                    "PaymentEvent: 결제 실패 - 메서드: {}, 이유: {}",
                    joinPoint.getSignature().getName(),
                    e.getMessage());
            throw e;
        }

        // 결제 성공
        if ("savePayment".equals(joinPoint.getSignature().getName())
                || "freePay".equals(joinPoint.getSignature().getName())) {

            Long reservationId;
            String paymentMethod;
            LocalDateTime approvedAt;
            String paymentKey;

            if ("savePayment".equals(joinPoint.getSignature().getName())) {
                JSONObject jsonObject = (JSONObject) joinPoint.getArgs()[0];
                String orderId = (String) jsonObject.get("orderId");
                reservationId = Long.parseLong(orderId.substring(23));
                paymentMethod = (String) jsonObject.get("method");
                approvedAt =
                        OffsetDateTime.parse((String) jsonObject.get("approvedAt"))
                                .toLocalDateTime();
                paymentKey = (String) jsonObject.get("paymentKey");
            } else {
                reservationId = (Long) joinPoint.getArgs()[0];
                paymentMethod = "0원 결제";
                approvedAt = LocalDateTime.now();
                paymentKey = null;
            }

            Payment payment =
                    paymentRepository
                            .findByReservationId(reservationId)
                            .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다"));

            log.info(
                    "PaymentEvent: 결제 완료 - 예매 ID: {}, 결제 수단: {}, 가격: {}, 승인 시간: {}, 결제 키: {}",
                    reservationId,
                    paymentMethod,
                    payment.getOriginAmount(),
                    approvedAt,
                    paymentKey);
        }
        // 결제 취소
        else if ("cancelPayment".equals(joinPoint.getSignature().getName())) {

            String paymentKey = (String) joinPoint.getArgs()[0];
            String cancelReason = (String) joinPoint.getArgs()[1];

            Payment payment =
                    paymentRepository
                            .findByPaymentKey(paymentKey)
                            .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

            log.info(
                    "PaymentEvent: 결제 취소 - 결제 ID: {}, 예매 ID: {}, 금액: {}, 시간: {}, 사유: {}",
                    payment.getId(),
                    payment.getReservationId(),
                    payment.getOriginAmount(),
                    LocalDateTime.now(),
                    cancelReason);
        } else {
            log.warn("PaymentEvent: 예상치 못한 메서드 - {}", result);
        }

        return result;
    }

    @Around("couponLog()")
    public Object couponLogstash(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            log.error(
                    "CouponEvent: 쿠폰 관련 실패 - 메서드: {}, 이유: {}",
                    joinPoint.getSignature().getName(),
                    e.getMessage());
            throw e;
        }

        // 쿠폰 생성
        if (result instanceof CreateCouponResponseDto responseDto) {
            log.info(
                    "CouponEvent: 생성 완료 - 쿠폰 ID: {}, 수량: {}, 타입: {}, 할인금액: {}, 유효기간 : {} ~ {}",
                    responseDto.getId(),
                    responseDto.getCurrentQuantity(),
                    responseDto.getType(),
                    responseDto.getDiscountValue(),
                    responseDto.getStartDate(),
                    responseDto.getEndDate());
        }
        // 쿠폰 삭제
        else if ("delete".equals(joinPoint.getSignature().getName())) {

            Long couponId = (Long) joinPoint.getArgs()[1];

            log.info("CouponEvent: 삭제 완료 - 쿠폰 ID: {}", couponId);
        }
        // 쿠폰 사용
        else if ("useCoupon".equals(joinPoint.getSignature().getName())) {
            Long couponId = (Long) joinPoint.getArgs()[0];
            Long userId = (Long) joinPoint.getArgs()[2];

            log.info(
                    "CouponEvent: 사용 완료 - 쿠폰 ID: {}, 유저 ID: {}, 사용일: {}",
                    couponId,
                    userId,
                    LocalDateTime.now());
        } else {
            log.warn("CouponEvent: 예상치 못한 메서드 - {}", result);
        }

        return result;
    }
}

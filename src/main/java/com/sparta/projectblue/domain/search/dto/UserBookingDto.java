package com.sparta.projectblue.domain.search.dto;

import java.time.LocalDateTime;

import com.sparta.projectblue.domain.common.enums.ReservationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserBookingDto {
    // 동기화와 검색에 공통으로 사용되는 필드
    private Long reservationId;
    private String userName;
    private Long userId;
    private String performanceTitle;
    private LocalDateTime bookingDate;
    private Long paymentAmount;
    private ReservationStatus reservationStatus;
    private Long paymentId;
    private LocalDateTime paymentDate;

    // 검색에만 사용되는 필드
    private LocalDateTime bookingDateStart;
    private LocalDateTime bookingDateEnd;
    private Long minPaymentAmount;
    private Long maxPaymentAmount;
    private String searchReservationStatus;

    @Builder
    public UserBookingDto(
            Long reservationId,
            String userName,
            Long userId,
            String performanceTitle,
            LocalDateTime bookingDate,
            Long paymentAmount,
            ReservationStatus reservationStatus,
            Long paymentId,
            LocalDateTime paymentDate) {
        this.reservationId = reservationId;
        this.userName = userName;
        this.userId = userId;
        this.performanceTitle = performanceTitle;
        this.bookingDate = bookingDate;
        this.paymentAmount = paymentAmount;
        this.reservationStatus = reservationStatus;
        this.paymentId = paymentId;
        this.paymentDate = paymentDate;
    }
}

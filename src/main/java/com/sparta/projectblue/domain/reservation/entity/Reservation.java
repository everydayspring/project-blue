package com.sparta.projectblue.domain.reservation.entity;

import com.sparta.projectblue.domain.common.entity.BaseEntity;
import com.sparta.projectblue.domain.common.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservation extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    private Long paymentId;

    @Column(nullable = false)
    private Long performanceId;

    @Column(nullable = false)
    private Long roundId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReservationStatus status;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int seatNumber;

    public Reservation(Long userId, Long performanceId, Long roundId, ReservationStatus status, int price, int seatNumber) {
        this.userId = userId;
        this.performanceId = performanceId;
        this.roundId = roundId;
        this.status = status;
        this.price = price;
        this.seatNumber = seatNumber;
    }

    public void addPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
}
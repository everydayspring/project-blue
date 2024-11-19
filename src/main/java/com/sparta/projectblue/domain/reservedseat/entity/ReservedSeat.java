package com.sparta.projectblue.domain.reservedseat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.sparta.projectblue.domain.common.entity.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "reserved_seats")
public class ReservedSeat extends BaseEntity {

    @Column(nullable = false, name = "reservation_id")
    private Long reservationId;

    @Column(nullable = false, name = "round_id")
    private Long roundId;

    @Column(nullable = false, name = "seat_number")
    private int seatNumber;

    public ReservedSeat(Long reservationId, Long roundId, int seatNumber) {
        this.reservationId = reservationId;
        this.roundId = roundId;
        this.seatNumber = seatNumber;
    }
}

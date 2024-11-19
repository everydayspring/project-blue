package com.sparta.projectblue.domain.reservation.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.projectblue.domain.common.enums.ReservationStatus;

import lombok.Getter;

@Getter
public class GetReservationsResponseDto {

    private final String performanceTitle;
    private final int tickets;
    private final Long reservationId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime reservationDate;

    private final String hallName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime round;

    private final ReservationStatus status;

    public GetReservationsResponseDto(
            String performanceTitle,
            int tickets,
            Long reservationId,
            LocalDateTime reservationDate,
            String hallName,
            LocalDateTime round,
            ReservationStatus status) {

        this.performanceTitle = performanceTitle;
        this.tickets = tickets;
        this.reservationId = reservationId;
        this.reservationDate = reservationDate;
        this.hallName = hallName;
        this.round = round;
        this.status = status;
    }
}

package com.sparta.projectblue.domain.reservation.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.projectblue.domain.common.enums.ReservationStatus;

import lombok.Getter;

@Getter
public class CreateReservationResponseDto {

    private final Long id;
    private final String performanceTitle;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime roundDate;

    private final List<Integer> seats;
    private final Long price;
    private final ReservationStatus status;

    public CreateReservationResponseDto(
            Long id,
            String performanceTitle,
            LocalDateTime roundDate,
            List<Integer> seats,
            Long price,
            ReservationStatus status) {

        this.id = id;
        this.performanceTitle = performanceTitle;
        this.roundDate = roundDate;
        this.seats = seats;
        this.price = price;
        this.status = status;
    }
}

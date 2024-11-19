package com.sparta.projectblue.domain.round.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

@Getter
public class GetRoundAvailableSeatsResponseDto {

    private final String performanceTitle;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime roundDate;

    private final List<Integer> seats;

    public GetRoundAvailableSeatsResponseDto(
            String performanceTitle, LocalDateTime roundDate, List<Integer> seats) {

        this.performanceTitle = performanceTitle;
        this.roundDate = roundDate;
        this.seats = seats;
    }
}

package com.sparta.projectblue.domain.reservation.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequestDto {

    @Schema(example = "2")
    @NotNull
    private Long roundId;

    @Schema(example = "[15, 16, 17]")
    @NotNull
    private List<Integer> seats;
}

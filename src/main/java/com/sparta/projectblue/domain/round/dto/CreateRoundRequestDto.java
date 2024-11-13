package com.sparta.projectblue.domain.round.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoundRequestDto {

    @Schema(example = "6")
    private Long performanceId;

    @Schema(example = "[\"2024-12-14T18:00:00\", \"2024-12-15T13:00:00\", \"2024-12-15T18:00:00\"]")
    private List<LocalDateTime> dates;
}

package com.sparta.projectblue.domain.review.dto;

import jakarta.validation.constraints.NotNull;

import com.sparta.projectblue.domain.common.enums.ReviewRate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequestDto {

    @Schema(example = "24")
    @NotNull
    private Long reservationId;

    @Schema(example = "ZERO, ONE, TWO, THREE, FOUR, FIVE")
    @NotNull
    private ReviewRate reviewRate;

    @Schema(example = "너무 재미있는 공연이었습니다")
    @NotNull
    private String contents;
}

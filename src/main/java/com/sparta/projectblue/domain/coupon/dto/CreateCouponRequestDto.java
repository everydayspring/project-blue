package com.sparta.projectblue.domain.coupon.dto;

import java.time.LocalDateTime;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import com.sparta.projectblue.domain.common.enums.CouponStatus;
import com.sparta.projectblue.domain.common.enums.CouponType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouponRequestDto {

    @Schema(example = "멋쟁이 전용 쿠폰")
    @NotNull
    private String couponCode;

    @Schema(example = "AMOUNT, RATE")
    @Enumerated(EnumType.STRING)
    @NotNull
    private CouponType type;

    @Schema(example = "ACTIVE, EXPIRED, USED")
    @Enumerated(EnumType.STRING)
    @NotNull
    private CouponStatus status;

    @Schema(example = "100")
    @NotNull
    private int maxQuantity;

    @Schema(example = "0")
    @NotNull
    private int currentQuantity;

    @Schema(example = "50")
    @NotNull
    private Long discountValue;

    @Schema(example = "2024-10-28T00:00:00")
    @NotNull
    private LocalDateTime startDate;

    @Schema(example = "2024-10-29T23:59:59")
    @NotNull
    private LocalDateTime endDate;
}

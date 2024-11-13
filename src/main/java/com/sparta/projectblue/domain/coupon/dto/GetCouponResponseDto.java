package com.sparta.projectblue.domain.coupon.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.projectblue.domain.common.enums.CouponStatus;
import com.sparta.projectblue.domain.common.enums.CouponType;
import com.sparta.projectblue.domain.coupon.entity.Coupon;

import lombok.Getter;

@Getter
public class GetCouponResponseDto {

    private final Long id;
    private final String code;
    private final CouponStatus status;
    private final CouponType type;
    private final int maxQuantity;
    private final int currentQuantity;
    private final Long discountValue;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime endDate;

    public GetCouponResponseDto(Coupon coupon) {

        this.id = coupon.getId();
        this.code = coupon.getCouponCode();
        this.status = coupon.getStatus();
        this.type = coupon.getType();
        this.maxQuantity = coupon.getMaxQuantity();
        this.currentQuantity = coupon.getCurrentQuantity();
        this.discountValue = coupon.getDiscountValue();
        this.startDate = coupon.getStartDate();
        this.endDate = coupon.getEndDate();
    }
}

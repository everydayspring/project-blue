package com.sparta.projectblue.domain.coupon.dto;

import java.time.LocalDateTime;

import com.sparta.projectblue.domain.common.enums.CouponStatus;
import com.sparta.projectblue.domain.common.enums.CouponType;
import com.sparta.projectblue.domain.coupon.entity.Coupon;

import lombok.Getter;

@Getter
public class CreateCouponResponseDto {

    private final Long id;
    private final String code;
    private final CouponStatus status;
    private final CouponType type;
    private final int maxQuantity;
    private final int currentQuantity;
    private final Long discountValue;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public CreateCouponResponseDto(Coupon coupon) {

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
